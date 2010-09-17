package com.fullmetalgalaxy.tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.fullmetalgalaxy.model.ModelFmpInit;
import com.fullmetalgalaxy.model.TokenType;
import com.fullmetalgalaxy.model.persist.AnBoardPosition;
import com.fullmetalgalaxy.model.persist.EbAccount;
import com.fullmetalgalaxy.model.persist.EbGame;
import com.fullmetalgalaxy.model.persist.EbRegistration;
import com.fullmetalgalaxy.model.persist.EbToken;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


/**
 * Goal of this tool, is to convert puzzles game between xml and binary format.
 * XML is used for human read and binary is used for FMG server.
 * As XML format is using xstream and GAE can't use it, I create this little workaround.
 * 
 * @author Vincent
 *
 */
public class Main
{

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    System.out.println( "FMG Tool started" );
    // createXml();
    gameXml2Bin( "../fullmetalgalaxy/war/puzzles/unpontontroploin/model.xml",
        "../fullmetalgalaxy/war/puzzles/unpontontroploin/model.bin" );
    // gameBin2Xml( "tutorial.bin" );
  }


  protected static void gameBin2Xml(String p_binFile)
  {
    FileInputStream fis = null;
    ObjectInputStream in = null;
    EbGame game = null;
    try
    {
      fis = new FileInputStream( new File( p_binFile ) );
      in = new ObjectInputStream( fis );
      game = EbGame.class.cast( in.readObject() );
      in.close();
      fis.close();
    } catch( Exception ex )
    {
      ex.printStackTrace();
    }

    XStream xstream = new XStream( new DomDriver() );
    xstream.toXML( game, System.out );
  }

  protected static void gameXml2Bin(String p_xmlFile, String p_binFile)
  {
    FileInputStream fis = null;
    EbGame game = null;
    try
    {
      fis = new FileInputStream( p_xmlFile );
      XStream xstream = new XStream( new DomDriver() );
      game = EbGame.class.cast( xstream.fromXML( fis ) );
      fis.close();
    } catch( Exception ex )
    {
      ex.printStackTrace();
    }
    if( game == null )
    {
      System.out.println( "Error: no game loaded" );
      return;
    }

    ModelFmpInit model = new ModelFmpInit();
    model.setGame( game );
    EbAccount account = new EbAccount();
    account.setId( 1 );
    account.setLogin( "Vous" );
    model.getMapAccounts().put( account.getId(), account );
    account = new EbAccount();
    account.setId( 2 );
    account.setLogin( "CPU" );
    model.getMapAccounts().put( account.getId(), account );
    
    FileOutputStream fos = null;
    ObjectOutputStream out = null;
    try
    {
      fos = new FileOutputStream( p_binFile );
      out = new ObjectOutputStream( fos );
      out.writeObject( model );
      out.close();
      fos.close();
    } catch( Exception ex )
    {
      ex.printStackTrace();
    }
  }

  protected static void createXml()
  {
    XStream xstream = new XStream( new DomDriver() );
    EbGame game = new EbGame();

    game.setLandSize( 10, 10 );

    EbToken token = new EbToken();
    token.setType( TokenType.Barge );
    game.addToken( token );
    game.moveToken( token, new AnBoardPosition( 1, 1 ) );

    EbToken tank = new EbToken();
    tank.setType( TokenType.Tank );
    game.addToken( tank );
    game.moveToken( tank, token );

    EbRegistration registration = new EbRegistration();
    game.addRegistration( registration );

    xstream.toXML( game, System.out );

  }

}
