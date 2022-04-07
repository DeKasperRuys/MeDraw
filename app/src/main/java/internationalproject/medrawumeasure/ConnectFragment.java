package internationalproject.medrawumeasure;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends DialogFragment {

    interface OnItemSelectedListener {
        void onComplete(BluetoothDevice device);
    }

    private OnItemSelectedListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnItemSelectedListener");
        }

    }

    private static final String TAG = "ConnectFragment";
    private static final int LOCATION_PERMISSION = 0;
    private static final int REQUEST_BLUETOOTH = 1;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private String connectedDeviceName = null;

    private BluetoothAdapter btAdapter;
    private ArrayList<BluetoothDevice> discoveryList = new ArrayList<>();
    ListView deviceList;
    private BluetoothDevice device;


    private ArrayAdapter<String> deviceArrayAdapter;


    public ConnectFragment() {
        // Required empty public constructor
    }

    static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter != null) {
            IntentFilter bluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);

            bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(btReceiver, bluetoothFilter);
        } else {
            //No support for bluetooth
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_connect, null, false);
        deviceArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        deviceList = view.findViewById(R.id.deviceList);

        deviceList.setAdapter(deviceArrayAdapter);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                device = discoveryList.get(i);
                mListener.onComplete(device);

                dismiss();
            }
        });


        if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
        } else {

            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
            }

        }


        if (btAdapter.isEnabled()) {
            scanBluetooth();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    //Bluetooth open
                } else {
                    Toast.makeText(getActivity(), "Bluetooth not enabled", Toast.LENGTH_SHORT);
                    dismiss();
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        getActivity().unregisterReceiver(btReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission granted.");
                    if (!btAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
                    }

                } else {

                    Log.i(TAG, "Permission denied.");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //Scan for bluetooth devices
    public void scanBluetooth() {
        Log.d(TAG, "scanBluetooth()");

        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        btAdapter.startDiscovery();

    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Bluetooth state changes
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "BL State changed");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "State OFF");
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;

                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "State ON");
                        scanBluetooth();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        deviceArrayAdapter.clear();
                        break;
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                discoveryList.clear();
                deviceArrayAdapter.clear();
                Log.i(TAG, "Discovery started");
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Discovery finished");
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!discoveryList.contains(device) && (device.getName() != null)) {
                    Log.d(TAG, "Found: " + device.toString());
                    discoveryList.add(device);
                    deviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            }

        }
    };

}
