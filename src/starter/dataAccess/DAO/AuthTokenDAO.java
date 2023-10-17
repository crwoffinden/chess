package dataAccess.DAO;

import dataAccess.model.AuthToken;
import dataAccess.model.User;

import java.util.HashMap;
import java.util.Map;

public class AuthTokenDAO {
    /**Map that will store the authtokens*/
    private Map<String, AuthToken> authtokens = new HashMap<>();

    /**Adds authtoken to the map
     *
     * @param authtoken
     */
    public void insert(AuthToken authtoken) {

    }

    /**Finds an authtoken by the authtoken string
     *
     * @param authtoken
     * @return
     */
    public AuthToken find(String authtoken){
        return null;
    }

    /**Deletes an authtoken from the table
     *
     * @param authtoken
     */
    public void remove(AuthToken authtoken) {

    }

    /**Clears the authtoken map
     *
     */
    public void clear(){

    }
}
