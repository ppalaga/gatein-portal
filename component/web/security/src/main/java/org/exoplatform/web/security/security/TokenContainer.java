/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.web.security.security;

import java.util.Map;
import java.util.Map.Entry;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.web.security.GateInToken;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "tkn:tokencontainer")
public abstract class TokenContainer {

    @Create
    protected abstract UserTokenCollection createUserTokenCollection();

    @OneToMany
    protected abstract Map<String, UserTokenCollection> getUserTokenCollections();

    public void cleanExpiredTokens() {
        Map<String, UserTokenCollection> users = getUserTokenCollections();
        for (UserTokenCollection userTokens : users.values()) {
            if (userTokens != null) {
                Map<String, TokenEntry> tokens = userTokens.getTokens();
                if (tokens != null) {
                    for (TokenEntry en : tokens.values()) {
                        GateInToken token = en.getToken();
                        if (token.isExpired()) {
                            en.remove();
                        }
                    }
                }
            }
        }
    }

    public int countTokens() {
        int result = 0;
        Map<String, UserTokenCollection> users = getUserTokenCollections();
        if (users != null) {
            for (Entry<String, UserTokenCollection> user : users.entrySet()) {
                UserTokenCollection userTokens = user.getValue();
                if (userTokens != null) {
                    result += userTokens.size();
                }
            }
        }
        return result;
    }

    public UserTokenCollection getUserTokenCollection(String user, boolean createIfNecessary) {
        Map<String, UserTokenCollection> users = getUserTokenCollections();
        UserTokenCollection userCollection = users.get(user);
        if (userCollection == null && createIfNecessary) {
            userCollection = createUserTokenCollection();
            users.put(user, userCollection);
        }
        return userCollection;
    }

    public void removeAll() {
        Map<String, UserTokenCollection> users = getUserTokenCollections();
        if (users != null) {
            for (Entry<String, UserTokenCollection> user : users.entrySet()) {
                user.getValue().remove();
            }
        }
    }

}
