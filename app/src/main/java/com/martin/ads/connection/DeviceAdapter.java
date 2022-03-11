package com.martin.ads.connection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.martin.ads.slamar.R;

import java.util.List;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private final List<WifiP2pDevice> wifiP2pDeviceList;


    public DeviceAdapter(List<WifiP2pDevice> wifiP2pDeviceList) {
        this.wifiP2pDeviceList = wifiP2pDeviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.tv_deviceName.setText(wifiP2pDeviceList.get(position).deviceName);
        holder.tv_deviceAddress.setText(wifiP2pDeviceList.get(position).deviceAddress);
        // holder.tv_deviceDetails.setText(WifiP2pUtils.getDeviceStatus(wifiP2pDeviceList.get(position).status));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return wifiP2pDeviceList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_deviceName;

        private final TextView tv_deviceAddress;

        // private final TextView tv_deviceDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tv_deviceName = itemView.findViewById(R.id.tv_deviceName);
            tv_deviceAddress = itemView.findViewById(R.id.tv_deviceAddress);
            // tv_deviceDetails = itemView.findViewById(R.id.tv_deviceDetails);
        }

    }

}
