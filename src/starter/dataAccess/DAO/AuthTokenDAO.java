package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;
import dataAccess.model.User;

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
        try {
            AuthToken otherAuthtoken = authtokens.get(authtoken.getAuthToken());
            if (otherAuthtoken != null) throw new DataAccessException("Authtoken already exists.");
        } finally {
            authtokens.put(authtoken.getAuthToken(), authtoken);
        }
    }

    /**Finds an authtoken by the authtoken string
     *
     * @param authtoken
     * @return
     * @throws DataAccessException
     */
    public AuthToken find(String authtoken) throws DataAccessException {
        AuthToken foundAuthtoken = authtokens.get(authtoken);
        if (foundAuthtoken == null) throw new DataAccessException("Authtoken not found.");
        return foundAuthtoken;
    }

    /**Deletes an authtoken from the table
     *
     * @param authtoken
     * @throws DataAccessException
     */
    public void remove(AuthToken authtoken) throws DataAccessException {
        boolean deleted = authtokens.remove(authtoken.getAuthToken(), authtoken);
        if (!deleted) throw new DataAccessException("Authtoken not found.");
    }

    /**Clears the authtoken map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {
        authtokens.clear();
    }
}
