package com.tanza.rufus;

import com.tanza.rufus.auth.BasicAuthenticator;
import com.tanza.rufus.auth.BasicAuthorizer;
import com.tanza.rufus.core.User;
import com.tanza.rufus.db.ArticleDao;
import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.resources.ArticleResource;
import com.tanza.rufus.resources.UserResource;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import org.skife.jdbi.v2.DBI;

/**
 * Created by jtanza.
 */
public class RufusApplication extends Application<RufusConfiguration> {

    public static void main(String[] args) throws Exception {
        new RufusApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<RufusConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/app", "/", "index.html"));
        bootstrap.addBundle(new MigrationsBundle<RufusConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(RufusConfiguration conf) {
                return conf.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(RufusConfiguration conf, Environment env) throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(env, conf.getDataSourceFactory(), "postgresql");

        /*
        ResultSet rs = RunScript.execute(jdbi.open().getConnection(), new FileReader("sourcejoin.sql"));
        while (rs.next()) {
            System.out.println(rs.getString("source"));
        }
        rs.close();
        */

        /*
        //init db
        RunScript.execute(jdbi.open().getConnection(), new FileReader("update.sql"));
        ResultSet execute = RunScript.execute(jdbi.open().getConnection(), new FileReader("sourcejoin.sql"));
        execute.close();
        */


        final UserDao userDao = jdbi.onDemand(UserDao.class);
        final ArticleDao articleDao = jdbi.onDemand(ArticleDao.class);
        env.jersey().register(new ArticleResource(userDao, articleDao));
        env.jersey().register(new UserResource(userDao));


        //route source
        env.jersey().setUrlPattern("/api/*");


        //security
        env.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new BasicAuthenticator(userDao))
                .setAuthorizer(new BasicAuthorizer())
                .setRealm("BASIC-AUTH-REALM").buildAuthFilter()));
        env.jersey().register(RolesAllowedDynamicFeature.class);
        env.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }
}