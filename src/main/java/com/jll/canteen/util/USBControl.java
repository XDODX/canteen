package com.jll.canteen.util;

import javax.usb.*;
import java.util.List;

public class USBControl {

    private static final int vendorId = 1155;
    private static final int productId = 4660;

    private static byte[] BASE_CMD = new byte[]{0x55, (byte) 0xaa, 0x01,
            (byte) 0xd1, 0x03, 0x30, 0x30, 0x30, (byte) 0x99, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    private UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId)
                return device;
            if (device.isUsbHub()) {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null)
                    return device;
            }
        }
        return null;
    }

    private void sendMessage(UsbDevice device, byte[] data) {
        try {
            UsbConfiguration config = device.getActiveUsbConfiguration();
            List totalInterfaces = config.getUsbInterfaces();
            for (int i = 0; i < totalInterfaces.size(); i++) {
                UsbInterface interf = (UsbInterface) totalInterfaces.get(i);
                try {
                    interf.claim();
                } catch (Exception e) {
                    continue;
                }
                List totalEndpoints = interf.getUsbEndpoints();
                for (int j = 0; j < totalEndpoints.size(); j++) {
                    UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(i);
                    UsbPipe pipe = ep.getUsbPipe();
                    pipe.open();
                    pipe.syncSubmit(data);
                    pipe.close();
                }
                interf.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callNo(int num) {
        try {
            UsbServices services = UsbHostManager.getUsbServices();
            UsbHub rootHub = services.getRootUsbHub();
            USBControl usbControl = new USBControl();
            UsbDevice u = usbControl.findDevice(rootHub, new Short(String.valueOf(vendorId)), new Short(String.valueOf(productId)));
            byte[] bytes;
            if (num >= 10)
                bytes = String.valueOf(num).getBytes();
            else
                bytes = ("0" + num).getBytes();
            byte[] data = USBControl.BASE_CMD;
            if (bytes.length == 1)
                data[7] = bytes[0];
            else if (bytes.length == 2) {
                data[6] = bytes[0];
                data[7] = bytes[1];
            }
            byte tmp = 0x00;
            for (int i = 2; i < 8; i++) {
                tmp += data[i];
            }
            data[8] = (byte) ~tmp;
            usbControl.sendMessage(u, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            callNo(24);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
