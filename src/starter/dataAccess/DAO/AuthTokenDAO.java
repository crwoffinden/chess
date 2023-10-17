package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;

import java.util.HashMap;
import java.util.Map;

/**Accesses and updates the authtoken table*/
public class AuthTokenDAO {
    /**Map that will store the authtokens*/
    private Map<String, AuthToken> authtokens = new HashMap<>();

    /**Adds authtoken to the map
     *
     * @param authtoken
     * @throws DataAccessException
     */
    public void insert(AuthToken authtoken) throws DataAccessException {

    }

    /**Finds an authtoken by the authtoken string
     *
     * @param authtoken
     * @return
     * @throws DataAccessException
     */
    public AuthToken find(String authtoken) throws DataAccessException {
        return null;
    }

    /**Deletes an authtoken from the table
     *
     * @param authtoken
     * @throws DataAccessException
     */
    public void remove(AuthToken authtoken) throws DataAccessException {

    }

    /**Clears the authtoken map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {

    }
}
