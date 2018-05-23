package com.keldee.svgp4.FileSystem;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.Route;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class RouteHolder {
    private ArrayList<Route> routes;
    private ArrayList<String> editedRoutes = new ArrayList<>();
    private Resources resources;
    private Context context;

    private String homeDirName;
    private String routeDirName;
    private final String routeExtensionName = "route";

    private File externalStorage;
    private File internalStorage;
    private File storage;
    private File homeDirectory;
    private File routeDirectory;

    private boolean externalStorageAvailable = false;
    private boolean externalStorageWritable = false;
    private boolean internalStorageAvailable = true;
    private boolean homeDirChecked = false;

    public String STORAGE_INTERNAL;
    public String STORAGE_EXTERNAL;

    public static String LOG;

    public RouteHolder (Context context) {
        this.context = context;
        this.resources = context.getResources();
        LOG = resources.getString(R.string.DEBUG_LOG_NAME);
        init();
    }

    public boolean isEverythingAlright () {
        return homeDirChecked;
    }

    private void init () {
        Log.d(LOG, "initializing RouteHolder...");
        homeDirName = resources.getString(R.string.FILE_HOMEDIR_NAME).replaceAll("\\s", "");
        routeDirName = resources.getString(R.string.FILE_ROUTEDIR_NAME);
        STORAGE_INTERNAL = resources.getString(R.string.STORAGE_INTERNAL);
        STORAGE_EXTERNAL = resources.getString(R.string.STORAGE_EXTERNAL);

        initInternalStorage();
        initExternalStorage();

        Log.d(LOG, "set storage to default: " + resources.getString(R.string.DEFAULT_STORAGE));
        switchStorage(resources.getString(R.string.DEFAULT_STORAGE));
        Log.d(LOG, "checking home directory...");
        checkHomeDir();
        if (!homeDirChecked) {
            switchStorage();
            checkHomeDir();
            if (!homeDirChecked) {
                //epic
                /*checkHomeDir();
                if (!homeDirChecked) {
                    checkHomeDir();
                    if (!homeDirChecked)
                        checkHomeDir();
                }*/
                Log.e(LOG, "after 2 tries home directory still not accessible");
                //TODO Make detailed check, if didn't work, tell user that there's something  with filesystem
                return;
            }
        }
        Log.d(LOG, "loading serialized routes from home directory...");
        routes = loadRoutesFromDevice();
        if (routes.isEmpty())
            Log.d(LOG, "didn't find any routes on device");
        while(routes.contains(null)) {
            int i = routes.indexOf(null);
            Log.d(LOG, "route with index:" + i + " wasn't loaded from device, removing");
            //TODO tell user that some route (maybe return empty route and show here its name) cannot be loaded
            routes.remove(i);
        }
    }

    public String print () {
        return getRouteNames().toString();
    }
    public String printEdited () {
        return editedRoutes.toString();
    }

    public ArrayList<String> getRouteNames () {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++)
            names.add(routes.get(i).name);

        return names;
    }

    public void onStop () {
        saveEditedRoutes();
    }

    public boolean addRoute (Route route) {
        if (getRouteIndex(route.name) < 0) {
            Log.d(LOG, "added new route \"" + route.name + "\"");
            routes.add(route);
            editedRoutes.add(route.name);
            return true;
        }
        else {
            Log.d(LOG, "route \"" + route.name + "\" already exists");
            //TODO Tell user that route with this name already exists
            return false;
        }
    }

    //TODO rewrite this sh*t
    public Route editRoute (String name) {
        Route r = getRoute(name);
        if (r == null)
            return null;
        if (!editedRoutes.contains(name)) {
            Log.d(LOG, "editing route: " + name);
            editedRoutes.add(r.name);
        }
        else {

            Log.d(LOG, "route:" + name + " is being edited now, so I'm going to break your program with yet another NullPointerException, my excuses");
            Log.d(LOG, "previous message is a joke");
            //commenting this line makes the app work sometimes
//            return null;
        }

        return r;
    }

    public void cancelEditRoute (String name) {
        Route r = getRoute(name);
        if (r != null && editedRoutes.contains(name)) {
            Log.d(LOG, "cancelled editing route: " + name);
            editedRoutes.remove(name);
        }
    }

    public boolean saveRoute (String name) {
        Route r = getRoute(name);
        if (r != null) {
            boolean res = saveRoute(routes.indexOf(r));
            if (editedRoutes.contains(r.name)) {
                editedRoutes.remove(name);
            }
            return res;
        }
        return false;
    }

    private boolean saveRoute (int index) {
        boolean result = false;
        Log.d(LOG, "saving route with index:" + index);
        if (!homeDirChecked)
            return result;
        if (index >= 0 && index < routes.size()) {
            Route r = routes.get(index);
            SerializeRoute s = new SerializeRoute().init(routeDirectory, routeExtensionName);
            try {
                result = s.execute(new Route[] {r}).get()[0];
                Log.d(LOG, "tried to save route:" + r.name + " result:" + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (editedRoutes.contains(r.name))
                editedRoutes.remove(r.name);
            return result;
        }
        return result;
    }

    private void saveEditedRoutes () {
        Log.d(LOG, "saving all edited routes...");
        int r;
        Iterator i = editedRoutes.iterator();

        while (i.hasNext()) {
            saveRoute((String) i.next());
        }
    }

    private int getRouteIndex (String name) {
        for (Route r : routes) {
            if (r.name.equals(name))
                return routes.indexOf(r);
        }
        return -1;
    }

    @Nullable
    private Route getRoute (String name) {
        for (Route r : routes) {
            if (r.name.equals(name))
                return r;
        }
        return null;
    }

    private ArrayList<Route> loadRoutesFromDevice () {
        ArrayList<Route> routes = new ArrayList<>();
        File[] routeFiles = routeDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return fileExtension(file).equals(routeExtensionName);
            }
        });
        if (routeFiles == null)
            Log.e(LOG, "routeDirectory.listFiles() returned null");
        else {
            DeserializeRoute d = new DeserializeRoute();
            try {
                routes = d.execute(routeFiles).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        return routes;
    }



    private boolean switchStorage (String newStorage) {
        if (newStorage.equals(STORAGE_INTERNAL)) {
            if (internalStorageAvailable) {
                storage = internalStorage;
                Log.d(LOG, "switched storage to: " + STORAGE_INTERNAL);
            }
            else {
                Log.wtf(LOG, "OOHHHGODDAMNWTFISGOINON?????? INTERNALSTORAGENOTAVAILABLE!!11!1!");
            }
        } else if (newStorage.equals(STORAGE_EXTERNAL)) {
            if (externalStorageAvailable && externalStorageWritable) {
                storage = externalStorage;
                Log.d(LOG, "switched storage to: " + STORAGE_EXTERNAL);
            } else {
                Log.e(LOG, "external storage isn't available, switching to internal");
                //TODO tell user that app cant work with external storage for some reasons
                switchStorage(STORAGE_INTERNAL);
                return false;
            }
        }
        return true;
    }

    public boolean switchStorage () {
        if (storage.equals(internalStorage))
            return switchStorage(STORAGE_EXTERNAL);
        else
            return storage.equals(externalStorage) && switchStorage(STORAGE_INTERNAL);
    }

    private void initInternalStorage () {
        internalStorage = context.getFilesDir();
    }

    private void initExternalStorage () {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            externalStorageAvailable = true;
            externalStorageWritable = true;
            externalStorage = Environment.getExternalStorageDirectory();
        }
        else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            externalStorageAvailable = true;
            externalStorage = Environment.getExternalStorageDirectory();
        }
        else {
            //TODO show user an error popup (cannot read/write external storage) then switch to private app storage
        }
    }

    private void checkHomeDir () {
        File homeDir;
        File routeDir;
        homeDir = new File(storage, homeDirName);
        if (homeDir.isDirectory()) {
            Log.d(LOG, "checked home directory: " + homeDir.getAbsolutePath());
            routeDir = new File(homeDir, routeDirName);
            if (routeDir.isDirectory()) {
                Log.d(LOG, "checked route directory: " + routeDir.getAbsolutePath());
                homeDirChecked = true;
                homeDirectory = homeDir;
                routeDirectory = routeDir;
            }
            else {
                if (!routeDir.mkdir()) {
                    Log.e(LOG, "cannot create route directory");
                    //TODO try to re-create
                    //TODO if not succeed throw critical exception application cant work with device storage work wont be saved
                }
                checkHomeDir();
            }
        }
        else {
            if (!homeDir.mkdir()) {
                Log.d(LOG, "cannot create home directory");
                //TODO try to re-create
                //TODO if not succeed throw critical exception application cant work with device storage work wont be saved
            }
            checkHomeDir();
        }
    }

    private String fileExtension (File file) {
        String path = file.getAbsolutePath();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0)
            return path.substring(dotIndex + 1);
        return "";
    }
}