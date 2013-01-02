/**
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.ContextualTask;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.web.security.GateInToken;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.AbstractCodecBuilder;
import org.exoplatform.web.security.hash.JCASaltedHashService;
import org.exoplatform.web.security.hash.SaltedHashException;
import org.exoplatform.web.security.hash.SaltedHashService;
import org.gatein.common.io.IOTools;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;

/**
 * <p>
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5, 2009
 * </p>
 * <p>
 * On 2013-01-02 the followig was added by ppalaga@redhat.com:
 * <ul>
 * <li>Passwords encrypted symmetrically before they are stored. The functionaliy was taken from <a
 * href="https://github.com/exoplatform/exogtn/commit/5ef8b0fa2d639f4d834444468426dfb2c8485ae9"
 * >https://github.com/exoplatform/exogtn/commit/5ef8b0fa2d639f4d834444468426dfb2c8485ae9</a> with minor modifications. See
 * {@link #codec}</li>
 * <li>The tokens are not stored in plain text, but intead only their salted hash is stored. See {@link #saltedHashService}. To
 * enable this, the following was done:
 * <ul>
 * <li>The structure of the underlying JCR store was changed from
 *
 * <pre>
 * autologin
 * |- plain-token1 user="user1" password="***" expiration="..."
 * |- plain-token2 user="user2" password="***" expiration="..."
 * `- ...
 * </pre>
 *
 * to
 *
 * <pre>
 * autologin
 * |- user1
 * |  |- plain-token1 user="user1" password="***" expiration="..."
 * |  |- plain-token2 user="user1" password="***" expiration="..."
 * |  `- ...
 * |- user2
 * |  |- plain-token3 user="user2" password="***" expiration="..."
 * |  |- plain-token4 user="user2" password="***" expiration="..."
 * |  `- ...
 * `- ...
 * </pre>
 *
 * </li>
 * <li>The value of the token was changed from {@code "rememberme" + randomString} to {@code userName + '.' + randomString}</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * It should be considered in the future if the password field can be removed altogether from {@link TokenEntry}.
 * </p>
 *
 */
public class CookieTokenService extends AbstractTokenService<GateInToken, String> {

    /** . */
    public static final String LIFECYCLE_NAME = "lifecycle-name";
    public static final String HASH_SERVICE_INIT_PARAM = "hash.service";

    /** . */
    private ChromatticLifeCycle chromatticLifeCycle;

    /** . */
    private String lifecycleName = "autologin";

    /**
     * {@link AbstractCodec} used to symmetrically encrypt passwords before storing them.
     */
    private AbstractCodec codec;

    private SaltedHashService saltedHashService;

    private final Logger log = LoggerFactory.getLogger(CookieTokenService.class);

    public CookieTokenService(InitParams initParams, ChromatticManager chromatticManager)
            throws TokenServiceInitializationException {
        super(initParams);

        ArrayList<?> serviceConfig = initParams.getValuesParam(SERVICE_CONFIG).getValues();
        if (serviceConfig.size() > 3) {
            lifecycleName = (String) serviceConfig.get(3);
        }
        this.chromatticLifeCycle = chromatticManager.getLifeCycle(lifecycleName);

        ObjectParameter hashServiceParam = initParams.getObjectParam(HASH_SERVICE_INIT_PARAM);
        if (hashServiceParam == null || hashServiceParam.getObject() == null) {
            /* the default */
            saltedHashService = new JCASaltedHashService();
        } else {
            saltedHashService = (SaltedHashService) hashServiceParam.getObject();
        }

        initCodec();
    }

    private void initCodec() throws TokenServiceInitializationException {
        String builderType = PropertyManager.getProperty("gatein.codec.builderclass");
        Map<String, String> config = new HashMap<String, String>();

        if (builderType != null) {
            // If there is config for codec in configuration.properties, we read the config parameters from config file
            // referenced in configuration.properties
            String configFile = PropertyManager.getProperty("gatein.codec.config");
            InputStream in = null;
            try {
                File f = new File(configFile);
                in = new FileInputStream(f);
                Properties properties = new Properties();
                properties.load(in);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    config.put((String) entry.getKey(), (String) entry.getValue());
                }
                config.put("gatein.codec.config.basedir", f.getParentFile().getAbsolutePath());
            } catch (IOException e) {
                throw new TokenServiceInitializationException("Failed to read the config parameters from file '" + configFile
                        + "'.", e);
            } finally {
                IOTools.safeClose(in);
            }
        } else {
            // If there is no config for codec in configuration.properties, we generate key if it does not exist and setup the
            // default config
            builderType = "org.exoplatform.web.security.codec.JCASymmetricCodecBuilder";
            String gtnConfDir = PropertyManager.getProperty("gatein.conf.dir");
            if (gtnConfDir == null || gtnConfDir.length() == 0) {
                throw new TokenServiceInitializationException("'gatein.conf.dir' property must be set.");
            }
            File f = new File(gtnConfDir + "/codec/codeckey.txt");
            if (!f.exists()) {
                File codecDir = f.getParentFile();
                if (!codecDir.exists()) {
                    codecDir.mkdir();
                }
                OutputStream out = null;
                try {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(128);
                    SecretKey key = keyGen.generateKey();
                    KeyStore store = KeyStore.getInstance("JCEKS");
                    store.load(null, "gtnStorePass".toCharArray());
                    store.setEntry("gtnKey", new KeyStore.SecretKeyEntry(key),
                            new KeyStore.PasswordProtection("gtnKeyPass".toCharArray()));
                    out = new FileOutputStream(f);
                    store.store(out, "gtnStorePass".toCharArray());
                } catch (Exception e) {
                    throw new TokenServiceInitializationException(e);
                } finally {
                    IOTools.safeClose(out);
                }
            }
            config.put("gatein.codec.jca.symmetric.keyalg", "AES");
            config.put("gatein.codec.jca.symmetric.keystore", "codeckey.txt");
            config.put("gatein.codec.jca.symmetric.storetype", "JCEKS");
            config.put("gatein.codec.jca.symmetric.alias", "gtnKey");
            config.put("gatein.codec.jca.symmetric.keypass", "gtnKeyPass");
            config.put("gatein.codec.jca.symmetric.storepass", "gtnStorePass");
            config.put("gatein.codec.config.basedir", f.getParentFile().getAbsolutePath());
        }

        try {
            this.codec = Class.forName(builderType).asSubclass(AbstractCodecBuilder.class).newInstance().build(config);
            log.info("Initialized CookieTokenService.codec using builder " + builderType);
        } catch (Exception e) {
            throw new TokenServiceInitializationException("Could not initialize CookieTokenService.codec.", e);
        }
    }

    public String createToken(final Credentials credentials) {
        if (validityMillis < 0) {
            throw new IllegalArgumentException();
        }
        if (credentials == null) {
            throw new NullPointerException();
        }
        final String user = credentials.getUsername();
        return new TokenTask<String>() {
            @Override
            protected String execute(SessionContext context) {
                String cookieTokenString = null;
                TokenContainer tokenContainer = getTokenContainer();
                while (cookieTokenString == null) {
                    String randomString = nextTokenId();
                    cookieTokenString = new CookieToken(user, randomString).toString();

                    String hashedRandomString = hashToken(randomString);
                    long expirationTimeMillis = System.currentTimeMillis() + validityMillis;

                    /* the symmetric encryption happens here */
                    String encryptedPassword = codec.encode(credentials.getPassword());
                    Credentials encodedCredentials = new Credentials(credentials.getUsername(), encryptedPassword);

                    UserTokenCollection userTokenCollection = tokenContainer.getUserTokenCollection(user, true);
                    try {
                        userTokenCollection.saveToken(hashedRandomString, encodedCredentials, new Date(expirationTimeMillis));
                    } catch (TokenExistsException e) {
                        cookieTokenString = null;
                    }
                }
                return cookieTokenString;
            }

        }.executeWith(chromatticLifeCycle);
    }

    @Override
    protected String nextTokenId() {
        return nextRandom();
    }

    @Override
    public GateInToken getToken(String cookieTokenString) {
        CookieToken token = null;
        try {
            token = new CookieToken(cookieTokenString);
            return new RemovableGetTokenTask(token, false).executeWith(chromatticLifeCycle);
        } catch (TokenParseException e) {
            log.warn("Could not parse cookie token.", e);
        }
        return null;
    }

    @Override
    public GateInToken deleteToken(String cookieTokenString) {
        CookieToken token = null;
        try {
            token = new CookieToken(cookieTokenString);
            return new RemovableGetTokenTask(token, true).executeWith(chromatticLifeCycle);
        } catch (TokenParseException e) {
            log.warn("Could not parse cookie token.", e);
        }
        return null;
    }

    @Override
    public void cleanExpiredTokens() {
        new TokenTask<Void>() {
            @Override
            protected Void execute(SessionContext context) {
                getTokenContainer().cleanExpiredTokens();
                return null;
            }
        }.executeWith(chromatticLifeCycle);
    }

    @Override
    public long size() {
        return new TokenTask<Long>() {
            @Override
            protected Long execute(SessionContext context) {
                return (long) getTokenContainer().countTokens();
            }
        }.executeWith(chromatticLifeCycle);
    }

    @Override
    protected String decodeKey(String stringKey) {
        return stringKey;
    }

    private String hashToken(String tokenId) {
        if (saltedHashService != null) {
            try {
                return saltedHashService.getSaltedHash(tokenId, random);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            /* no hash if saltedHashService is null */
            return tokenId;
        }
    }

    /**
     * Wraps token store logic conveniently.
     *
     * @param <V> the return type
     */
    private abstract class TokenTask<V> extends ContextualTask<V> {

        protected final TokenContainer getTokenContainer() {
            SessionContext ctx = chromatticLifeCycle.getContext();
            ChromatticSession session = ctx.getSession();
            TokenContainer container = session.findByPath(TokenContainer.class, lifecycleName);
            if (container == null) {
                container = session.insert(TokenContainer.class, lifecycleName);
            }
            return container;
        }

    }

    private class RemovableGetTokenTask extends TokenTask<GateInToken> {
        private final CookieToken token;
        private final boolean remove;

        /**
         * @param token
         */
        public RemovableGetTokenTask(CookieToken token, boolean remove) {
            super();
            this.token = token;
            this.remove = remove;
        }

        @Override
        protected GateInToken execute(SessionContext context) {
            UserTokenCollection userCollection = getTokenContainer().getUserTokenCollection(token.getUser(), false);
            if (userCollection != null) {
                Map<String, TokenEntry> userTokenEntries = userCollection.getTokens();
                if (userTokenEntries != null) {
                    for (Entry<String, TokenEntry> hashTokenEntry : userTokenEntries.entrySet()) {
                        try {
                            if (saltedHashService.validate(token.getRandomString(), hashTokenEntry.getKey())) {
                                TokenEntry tokenEntry = hashTokenEntry.getValue();
                                GateInToken encryptedToken = tokenEntry.getToken();
                                Credentials encryptedCredentials = encryptedToken.getPayload();
                                Credentials decryptedCredentials = new Credentials(encryptedCredentials.getUsername(),
                                        codec.decode(encryptedCredentials.getPassword()));
                                if (remove) {
                                    tokenEntry.remove();
                                }
                                return new GateInToken(encryptedToken.getExpirationTimeMillis(), decryptedCredentials);
                            }
                        } catch (SaltedHashException e) {
                            log.warn("Could not validate cookie token against its salted hash.", e);
                        }
                    }
                }
            }
            return null;
        }
    }
}
