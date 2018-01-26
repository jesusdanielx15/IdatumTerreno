package cl.idatum.idatumterreno.app;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.concurrent.atomic.AtomicInteger;

import cl.idatum.idatumterreno.models.CambioEstadosObject;
import cl.idatum.idatumterreno.models.Contenidos;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Aplicacion que se encarga de configurar la base de datos y regresar el ultimo id
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class MyApplication extends Application {

    public static AtomicInteger ContenidosId = new AtomicInteger();
    public static AtomicInteger cambioEstadosId = new AtomicInteger();

    /**
     * Metodo principal de la clase.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        setUpRealmConfig();
        Realm realm = Realm.getDefaultInstance();
        ContenidosId = getIdByTable(realm, Contenidos.class);
        cambioEstadosId = getIdByTable(realm, CambioEstadosObject.class);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

        realm.close();
    }

    /**
     * Configura la base de datos Realm.
     */
    private void setUpRealmConfig() {
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(configuration);
    }

    /**
     * Encuentra el ultimo id de la base de datos.
     * @param realm es la instancia de la base de datos.
     * @param anyClass La base de datos a la cual se conseguira el ultimo Id.
     * @param <T>
     * @return retorna el ultimo id de la base de datos.
     */
    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass) {
        RealmResults<T> results = realm.where(anyClass).findAll();
        if (results.size() > 0) {

            return new AtomicInteger(results.max("id").intValue());
        } else return new AtomicInteger();

    }
}
