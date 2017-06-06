package com.accenture.demoalta;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.sax.EndElementListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyA081GZ_FetOvIH4lCfDSgZHU-Hn-MIM6E";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    public ImageView mImage;
    public EditText mNombre;
    public EditText mDomicilio;
    public EditText mCurp;
    public TextView mMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionsBtn();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,  R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mImage = (ImageView) findViewById(R.id.image);
        mNombre = (EditText) findViewById(R.id.nombre);
        //mNombre.setFocusable(false);
       mNombre.setEnabled(false);
        mDomicilio = (EditText) findViewById(R.id.domicilio);
        mDomicilio.setEnabled(false);
        mCurp = (EditText) findViewById(R.id.curp);
        mCurp.setEnabled(false);
        mMensaje = (TextView) findViewById(R.id.mensajes);


    }
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Selecciona una foto "),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    //Método si elige tomar una foto desde cámara
    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    //método que obtiene la imagen de origen
    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    //Método prepara imagen para procesamiento
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    //Carga y da formato a la imagen para su procesamiento,
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                callCloudVision(bitmap);
                mImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        mMensaje.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            public String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();


                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature documentText = new Feature();
                            Feature facialDetection = new Feature();
                            //
                            documentText.setType("DOCUMENT_TEXT_DETECTION");
                            facialDetection.setType("FACE_DETECTION");
                            //
                            add(documentText);
                            //
                            add(facialDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                   return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Asegurate de tener conexión a internet.";
            }
            protected void onPostExecute(String result){
                if (result!=null){

                    Log.d(TAG,"Recupera datos final de método onPost-- INICIO"+result);
                    mMensaje.setText(result);

                    if (result.contains(",")){
                        result.length();
                        Log.d(TAG,"tamaño" + result.length());
                        int iComa = result.indexOf(",");
                        int iSlash = result.indexOf("/");

                        String nom =result.substring(0,iComa);
                        String com = result.substring(iComa +1,iSlash-1);
                        String curp = result.substring(iSlash +1,iSlash +20);
                        Log.d(TAG,"Tamaño curp" + curp);

                        mNombre.setText(nom);
                        mDomicilio.setText(com);
                        mCurp.setText(curp);
                        mMensaje.setText(R.string.final_message);
                    }
                    mCurp.setEnabled(true);
                    mCurp.isCursorVisible();
                    mNombre.setEnabled(true);
                    mNombre.isCursorVisible();
                    mDomicilio.setEnabled(true);
                    mDomicilio.isCursorVisible();
                }else{
                    mMensaje.setText(R.string.image_picker_error);
                }
            }
        }.execute();
    }


    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";
        String sNombreCmp ="";
        String sDom  ="";
        String sCurp ="";
        String fMessage = "";

        Log.d(TAG,"Valida los tipos de response");
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            int i = 0;
            for (EntityAnnotation text : texts) {
                message +=text.getDescription();
                i++;
                Log.d(TAG,"Numero: " + i + "Response de TEXT DETECTION: " + text );
            }
            if (message.contains("NOMBRE")&&message.contains("DOMICILIO")&&message.contains("CURP")||
                    message.contains("FOLIO")&&message.contains("CLAVE")&&message.contains("ESTA")) {
                int pocisionNombre = message.indexOf("NOMBRE");
                int posDomi = message.indexOf("DOMICILIO");
                int posicionClave = message.indexOf("CLAVE");

                Log.d(TAG, "pocisionNombre: " + pocisionNombre + " posDomi: " + posDomi);

                int posFolio = message.indexOf("FOLIO");
                int posCurp = message.indexOf("CURP");
                int posAnio = message.indexOf("ANO");
                int posEstado = message.indexOf("ESTA");


                /************************************************/
                /************** Formato para Nombre *************/
                /************************************************/
                Formatter formatter = new Formatter();
                sNombreCmp = formatter.obtenerCadenaNombre(message);

                if(formatter.contieneFecha(sNombreCmp)){
                    sNombreCmp= formatter.quitarFecha(sNombreCmp);
                }
                sNombreCmp = formatter.quitarEtiquetasIFE(sNombreCmp);
                Log.d(TAG, "Nombre: " +sNombreCmp);
                /************************************************/
                /************ FIN Formato para Nombre ***********/
                /************************************************/

                //obtiene domicilio
                if (message.contains("FOLIO")) {
                    sDom += message.substring(posDomi, posFolio);
                    Log.d(TAG, "domicilio " + sDom);
                } else {
                    sDom = message.substring(posDomi, posicionClave);
                    Log.d(TAG, "Domicilio 1 " + sDom);
                }
                sDom = formatter.quitarEtiquetasIFE(sDom);
                Log.d(TAG, "Domicilio fin " + sDom);

                //obtiene curp
                if (message.contains("CURP")) {
                    if (message.contains("FOLIO")) {
                        sCurp = message.substring(posCurp, posEstado);
                        Log.d(TAG, "CURP1 " + sCurp);
                    } else {
                        sCurp += message.substring(posCurp, posAnio);
                        Log.d(TAG, "CURP2 " + sCurp);
                    }
                    sCurp = formatter.quitarEtiquetasIFE(sCurp);
                    Log.d(TAG, "CURP final " + sCurp);
                }
            }//fin de if que valida entrada
            else{
                fMessage +="Algo sucedió con la imagen. Por favor vuelve a capturar.";
                Log.d(TAG,"Error con la imagen, baja resolución");
            }
        } else {
            fMessage +="Algo sucedió con la imagen. Selecciona otra.";
            Log.d(TAG,"Error con la imagen");
        }
        fMessage += sNombreCmp +","+ sDom +"/"+ sCurp;

        Log.d(TAG,"final " + fMessage);
        return fMessage;
    }


    public void actionsBtn(){
        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_message)
                        .setPositiveButton(R.string.dialog_select_ok, new DialogInterface.OnClickListener() {
                            //el usuario eligió una imagen de galería
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeContextMenu();
                            }
                        }
                       );
                builder.create().show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNombre.setText("");
                mDomicilio.setText("");
                mCurp.setText("");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            //el usuario eligió una imagen de galería
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            //el usuario elige una imagen con cámara del dispositivo
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // item para menú de navegación izdo.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
           //Acciones para cada item
        } else if (id == R.id.nav_tuto) {

        } else if (id == R.id.nav_contact) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
