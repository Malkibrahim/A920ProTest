package com.demoa920pro;

 import android.Manifest;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Build;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;

 import androidx.core.app.ActivityCompat;
 import androidx.core.content.ContextCompat;

 import com.facebook.react.bridge.Callback;
 import com.facebook.react.bridge.ReactApplicationContext;
 import com.facebook.react.bridge.ReactContextBaseJavaModule;
 import com.facebook.react.bridge.ReactMethod;
 import com.pax.dal.IDAL;
 import com.pax.dal.IPrinter;
 import com.pax.dal.exceptions.PrinterDevException;
 import com.pax.gl.page.IPage;
 import com.pax.gl.page.PaxGLPage;
 import com.pax.neptunelite.api.NeptuneLiteUser;



public class PrintModule extends ReactContextBaseJavaModule {
    public static NeptuneLiteUser neptuneLiteUser;
    public static IDAL dal;
    public static PaxGLPage paxGLPage;
    public static IPrinter printer;

    String serialNumber = "";
    String name = "";
    String type = "";
    String title = "";
    String hQ = "";
    Callback printSuccess = null;
    Callback printFail = null;

     Callback serialSuccessCallback = null;
    Callback serialFailCallback = null;
    public PrintModule(ReactApplicationContext reactContext) {
        super(reactContext); //required by React Native

    }


    @Override
    public String getName() {
        return "PrintModule";
    }


    @ReactMethod
    public void getSerialNumber(Callback success, Callback fail) {
        serialFailCallback = fail;
        serialSuccessCallback = success;
        if (Build.VERSION.SDK_INT <= 25) {
            serialNumber = Build.SERIAL;
            success.invoke(serialNumber);
        } else if (Build.VERSION.SDK_INT == 26) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getReactApplicationContext(),
                    Manifest.permission.READ_PHONE_STATE
            );

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        getCurrentActivity(),
                        new String[] { Manifest.permission.READ_PHONE_STATE },
                        10
                );
            } else {
                serialNumber = Build.getSerial().toString();

                success.invoke(serialNumber);
            }
        }
    }
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults
    ) {
        switch (requestCode) {
            case 10:
                if (
                        (grantResults.length > 0) &&
                                (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    serialNumber = Build.getSerial().toString();
                    serialSuccessCallback.invoke(serialNumber);
                } else {
                    serialFailCallback.invoke("Fail");
                }
                break;
            default:
                break;
        }
    }


    @ReactMethod
    public void PrintReceipt(String name,String type, String title, String hQ,Callback printSuccess,Callback printFail) {
//        String name,String type, String title, String hQ,
            //Get the entity of printer.
            neptuneLiteUser = NeptuneLiteUser.getInstance();
            try {
                dal = neptuneLiteUser.getDal(this.getReactApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("getDal", e.toString());
            }

            paxGLPage = PaxGLPage.getInstance(this.getReactApplicationContext());

            printer = dal.getPrinter();

        this.name = name;
        this.type = type;
        this.title = title;
        this.name = hQ;
this.printSuccess=printSuccess;
        this.printFail=printFail;

//      this.zoneName=zoneName
            //successCallback.invoke(printer.toString());

            print_img();

    }

    private Bitmap generate() {
        IPage page = paxGLPage.createPage();

        page.adjustLineSpace(8); //default 0
        //To set the font file
        //    page.setTypeFace("/data/resource/font/DroidSansFallback.ttf");


        page.addLine().addUnit(
                        name,
                        26,
                        IPage.EAlign.LEFT,
                        IPage.ILine.IUnit.TEXT_STYLE_BOLD
                );
        page.addLine().addUnit(
                        type,
                        28,
                        IPage.EAlign.RIGHT
                );

        page.addLine().addUnit(title, 28);


            page.addLine().addUnit(hQ, 28, IPage.EAlign.CENTER);





        page.addLine().addUnit("\n" + "Powered by Damen", 18, IPage.EAlign.LEFT);
        //

        page.addLine().addUnit("\n", 30);

        return paxGLPage.pageToBitmap(page, 385);
    }

    private void print_img() {
        printBitmap(generate());
    }

    public void init() {
        try {
            printer.init();
        } catch (PrinterDevException e) {
            e.printStackTrace();
            Log.d("init", e.toString());
        }
    }

    protected void printBitmap(Bitmap bitmap) {
        init();
        try {
            printer.printBitmap(bitmap);
        } catch (PrinterDevException e) {
            printFail.invoke("Fail");
            e.printStackTrace();
            Log.d("printBitmap", e.toString());
        }
        start(printer);
    }
    public void onShowMessage(final String message) {
        //        handler.post(new Runnable() {
        //            @Override
        //            public void run() {
        //                Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
        //            }
        //        });

        Toast
                .makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }
    private int start(IPrinter printer) {
        try {
            int ret = printer.start();
            // printer is busy, please wait
            if (ret == 1) {
                onShowMessage("برجاء الانتظار ......");
            } else if (ret == 2) {
                onShowMessage("لا يوجد ورق لطباعة الايصال");
                printFail.invoke("Fail");
                return -1;
            } else if (ret == 8) {
                onShowMessage("برجاء اغلاق الماكينة قليلا ثم اعادة فتحها مرة اخرى");
                printFail.invoke("Fail");
                return -1;
            } else if (ret == 9) {
                onShowMessage("برجاء شحن الماكينة");
                printFail.invoke("Fail");
                return -1;
            } else if (ret != 0) {
                return -1;
            }

            onShowMessage("تم طباعة الايصال بنجاح");
            printSuccess.invoke("Success");
            return 0;
        } catch (Exception ex) {
            return 0;
        }
    }


}
