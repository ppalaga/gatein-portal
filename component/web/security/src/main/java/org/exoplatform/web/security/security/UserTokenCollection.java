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

import java.util.Date;
import java.util.Map;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.web.security.GateInToken;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.gatein.wci.security.Credentials;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
@PrimaryType(name = "tkn:usertokencollection")
public abstract class UserTokenCollection {

    @Create
    protected abstract TokenEntry createToken();

    @OneToMany
    public abstract Map<String, TokenEntry> getTokens();

    @Name
    public abstract String getId();

    @Destroy
    public abstract void remove();

    public GateInToken getToken(String tokenId) {
        Map<String, TokenEntry> tokens = getTokens();
        TokenEntry entry = tokens.get(tokenId);
        return entry != null ? entry.getToken() : null;
    }

    public GateInToken removeToken(String tokenId) {
        Map<String, TokenEntry> tokens = getTokens();
        TokenEntry entry = tokens.get(tokenId);
        if (entry != null) {
            GateInToken token = entry.getToken();
            entry.remove();
            return token;
        } else {
            return null;
        }
    }

    public void saveToken(String hashedToken, Credentials credentials, Date expirationTime) throws TokenExistsException {
        Map<String, TokenEntry> tokens = getTokens();
        if (tokens.containsKey(hashedToken)) {
            throw new TokenExistsException();
        }
        TokenEntry entry = createToken();
        tokens.put(hashedToken, entry);
        entry.setUserName(credentials.getUsername());
        entry.setPassword(credentials.getPassword());
        entry.setExpirationTime(expirationTime);
    }

    public GateInToken getTokenAndDecode(String tokenId, AbstractCodec codec) {
        Map<String, TokenEntry> tokens = getTokens();
        TokenEntry entry = tokens.get(tokenId);
        if (entry != null) {
            GateInToken gateInToken = entry.getToken();
            Credentials payload = gateInToken.getPayload();

            // Return a cloned GateInToken
            return new GateInToken(gateInToken.getExpirationTimeMillis(), new Credentials(payload.getUsername(),
                    codec.decode(payload.getPassword())));

        }
        return null;
    }

    /**
     * @return
     */
    public int size() {
        Map<String, TokenEntry> tokens = getTokens();
        return tokens != null ? tokens.size() : 0;
    }

}
