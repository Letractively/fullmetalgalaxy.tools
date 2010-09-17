/**
 * 
 */
package com.fullmetalgalaxy.server.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.fullmetalgalaxy.model.LandType;
import com.fullmetalgalaxy.model.PlanetType;
import com.fullmetalgalaxy.model.TokenType;
import com.fullmetalgalaxy.model.constant.ConfigGameTime;
import com.fullmetalgalaxy.model.constant.ConfigGameVariant;
import com.fullmetalgalaxy.model.persist.AnBoardPosition;
import com.fullmetalgalaxy.model.persist.EbGame;
import com.fullmetalgalaxy.model.persist.EbToken;
import com.fullmetalgalaxy.server.FmpLogger;
import com.fullmetalgalaxy.server.datastore.FmgDataStore;

/**
 * @author Vincent Legendre
 *
 */
public class GameXmlServlet extends HttpServlet
{
  static final long serialVersionUID = 890;

  /**
   * The log channel
   */
  private final static FmpLogger log = FmpLogger.getLogger( GameXmlServlet.class.getName() );


  /**
   * servlet initialisation
   * initialize SGBD connexion.
   */
  @Override
  public void init() throws ServletException
  {
    super.init();
  }



  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected final void doGet(HttpServletRequest p_req, HttpServletResponse p_resp)
      throws ServletException, IOException
  {

  }

  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected final void doPost(HttpServletRequest p_req, HttpServletResponse p_resp)
      throws ServletException, IOException
  {
    ServletFileUpload upload = new ServletFileUpload();
    Map<String, String> params = new HashMap<String, String>();

    try
    {
      // Parse the request
      FileItemIterator iter = upload.getItemIterator( p_req );
      while( iter.hasNext() )
      {
        FileItemStream item = iter.next();
        if( item.isFormField() )
        {
          // this is a form param
          params.put( item.getFieldName(), Streams.asString( item.openStream() ) );
        }
        else if( item.getFieldName().equalsIgnoreCase( "gamefile" ) )
        // getContentType().startsWith( "text/xml" ) )
        {
          // this is an xml file
          EbGame game = createGame( item.openStream() );
          if( game != null )
          {
            FmgDataStore store = new FmgDataStore();
            store.save( game );
            store.close();
          }
          break;
        }
      }
    } catch( FileUploadException e )
    {
      log.error( e );
    }
  }


  private String readln(InputStream p_input)
  {
    int ch = 0;
    StringBuffer output = new StringBuffer();
    try
    {
      ch = p_input.read();
      while( ch >= 0 && ch != '\n' )
      {
        output.append( (char)ch );
        ch = p_input.read();
      }
    } catch( IOException e )
    {
      log.error( e );
    }
    return output.toString();
  }

  private EbGame createGame(InputStream p_input)
  {
    EbGame game = new EbGame();
    game.setName( "upload" );
    game.setPlanetType( PlanetType.Desert );
    game.setAsynchron( false );
    game.setConfigGameTime( ConfigGameTime.Standard );
    game.setConfigGameVariant( ConfigGameVariant.Standard );
    game.setMaxNumberOfPlayer( 4 );

    String str = readln( p_input );
    if( str == null || !str.startsWith( "<FMP Format=4 Version=\"2.3.7" ) )
    {
      return null;
    }
    // lecture de la carte
    str = readln( p_input );
    if( str == null || !str.startsWith( "<CARTE Lignes=" ) )
    {
      return null;
    }
    int height = Integer.parseInt( str.substring( 14, 16 ) );
    int width = Integer.parseInt( str.substring( 26, 28 ) );
    game.setLandSize( width, height );
    for( int iy = 0; iy < height; iy++ )
    {
      str = readln( p_input );
      for( int ix = 0; ix < width; ix++ )
      {
        switch( str.charAt( ix ) )
        {
        case '.':
          game.setLand( ix, iy, LandType.Sea );
          break;
        case '*':
          game.setLand( ix, iy, LandType.Reef );
          break;
        case '%':
          game.setLand( ix, iy, LandType.Marsh );
          break;
        case '$':
          game.setLand( ix, iy, LandType.Plain );
          break;
        case '#':
          game.setLand( ix, iy, LandType.Montain );
          break;
        case '?':
        default:
          game.setLand( ix, iy, LandType.None );
          break;
        }
      }
    }
    str = readln( p_input );
    if( str == null || !str.startsWith( "</CARTE>" ) )
    {
      return null;
    }
    // lecture des minerais
    str = readln( p_input );
    if( str == null || !str.startsWith( "<REPARTITION Lignes=" ) )
    {
      return null;
    }
    height = Integer.parseInt( str.substring( 20, 22 ) );
    width = Integer.parseInt( str.substring( 32, 34 ) );
    for( int iy = 0; iy < height; iy++ )
    {
      str = readln( p_input );
      for( int ix = 0; ix < width; ix++ )
      {
        if(str.charAt( ix ) != '.')
        {
          EbToken token = new EbToken( TokenType.Ore );
          game.addToken( token );
          game.moveToken( token, new AnBoardPosition( ix, iy ) );
        }
      }
    }
    str = readln( p_input );
    if( str == null || !str.startsWith( "</REPARTITION>" ) )
    {
      return null;
    }

    try
    {
      p_input.close();
    } catch( IOException e )
    {
      log.error( e );
    }

    return game;
  }

  /*
  private EbGame createGame(InputStream p_input)
  {
    EbGame game = new EbGame();

    DocumentBuilderFactory factory = null;
    Document document = null;
    try
    {
      factory = DocumentBuilderFactory.newInstance();
      factory.setValidating( false );
      DocumentBuilder builder = factory.newDocumentBuilder();
      // document = builder.parse( correctFmpFile( item.openStream() ) );
      document = builder.parse( p_input );
    } catch( Exception e )
    {
      log.error( e );
    }
    System.out.print( document.getDocumentElement().getFirstChild().getAttributes() );

    System.out.print( document.getChildNodes() );
    Element elemFmp = document.getElementById( "FMP" );
    System.out.print( elemFmp.getAttribute( "Format" ) );
    Element elemCarte = document.getElementById( "CARTE" );
    System.out.print( elemCarte.getAttribute( "Lignes" ) );
    System.out.print( elemCarte.getAttribute( "Colonnes" ) );

    return game;
  }

  private InputStream correctFmpFile(InputStream p_input)
  {
    int ch = 0;
    boolean isCreatingString = false;
    char lastChar = 0;
    StringBuffer output = new StringBuffer();
    try
    {
      ch = p_input.read();
      while( ch >= 0 )
      {
        if( lastChar == '=' && ch != '"' )
        {
          isCreatingString = true;
          output.append( '"' );
        }
        if( (isCreatingString == true) && (ch == ' ' || ch == '>') )
        {
          isCreatingString = false;
          output.append( '"' );
        }
        if( ch == '=' )
        {
          lastChar = '=';
        }
        else
        {
          lastChar = 0;
        }
        output.append( (char)ch );
        ch = p_input.read();
      }
    } catch( IOException e )
    {
      log.error( e );
    }
    System.out.print( output );
    return new ByteArrayInputStream( new String( output ).getBytes() );
  }
  */


}
