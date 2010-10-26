/* *********************************************************************
 *
 *  This file is part of Full Metal Galaxy.
 *  http://www.fullmetalgalaxy.com
 *
 *  Full Metal Galaxy is free software: you can redistribute it and/or 
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 *
 *  Full Metal Galaxy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public 
 *  License along with Full Metal Galaxy.  
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2010 Vincent Legendre
 *
 * *********************************************************************/
package com.fullmetalgalaxy.tools;

import java.io.InputStream;
import java.io.OutputStream;

import com.fullmetalgalaxy.model.ModelFmpInit;
import com.fullmetalgalaxy.model.persist.EbAccount;
import com.fullmetalgalaxy.model.persist.EbGame;
import com.fullmetalgalaxy.model.persist.EbRegistration;

/**
 * @author vlegendr
 *
 */
public abstract class DriverFileFormat
{
  public abstract ModelFmpInit loadGame(InputStream p_input);
  
  
  public abstract void saveGame(ModelFmpInit p_game, OutputStream p_output);
  
  
  protected ModelFmpInit game2Model(Object p_game)
  {
    if(p_game instanceof EbGame)
    {
      return game2Model( EbGame.class.cast( p_game ) );
    }
    if(p_game instanceof ModelFmpInit)
    {
      return ModelFmpInit.class.cast( p_game );
    }
    return null;
  }
  
  protected ModelFmpInit game2Model(EbGame p_game)
  {
    ModelFmpInit model = new ModelFmpInit();
    model.setGame( p_game );
    
    int id = 1;
    EbAccount account = null;
    for(EbRegistration registration : p_game.getSetRegistration() )
    {
      account = new EbAccount();
      account.setId( id );
      switch(id)
      {
      case 1:
        account.setLogin( "Vous" );
      case 2:
        account.setLogin( "CPU" );
      default:
        account.setLogin( "CPU-"+(id-1) );
      }
      model.getMapAccounts().put( account.getId(), account );
    }
    return model;
  }
}
