package cl.idatum.idatumterreno.ServicioNotificationPush;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.activities.CambioEstados;
import cl.idatum.idatumterreno.models.CambioEstadosObject;
import cl.idatum.idatumterreno.utils.Config;
import io.realm.Realm;


/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 22/1/2018
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private Realm realm;
    private SharedPreferences prefs;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        showNotification(remoteMessage.getData().get("obj_nombre") + ", " +
                remoteMessage.getData().get("estado_anterior") + " - " +
                remoteMessage.getData().get("estado_actual"), (int) (Math.random() * 1000));

        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        realm = Realm.getDefaultInstance();

        if (prefs.getString(Config.ID_USUARIO_SHARED, "") != "") {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    CambioEstadosObject cambioEstadosObject = new CambioEstadosObject(prefs.getString(Config.ID_USUARIO_SHARED, ""),
                            remoteMessage.getData().get("id_msg"),
                            remoteMessage.getData().get("obj_nombre"),
                            remoteMessage.getData().get("obj_id"),
                            remoteMessage.getData().get("estado_actual"),
                            remoteMessage.getData().get("estado_anterior"),
                            remoteMessage.getData().get("estado_actual_color"),
                            remoteMessage.getData().get("estado_anterior_color"),
                            remoteMessage.getData().get("fecha_cambio_estado"),
                            false);
                    realm.copyToRealm(cambioEstadosObject);
                }
            });
        }

    }

    private void showNotification(String message, int x) {

        Log.d("NOTIFICATION", message + " numero " + x);
        Intent i = new Intent(this, CambioEstados.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Idatum Terreno")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSubText("Cambio de estado")
                .setSmallIcon(R.drawable.ic_action_nav)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(x, builder.build());
    }
}
