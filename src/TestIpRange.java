
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestIpRange {


   private final String cidr;

   public ArrayList<String[]> storage = new ArrayList<>();
   private InetAddress inetAddress;
   private InetAddress startAddress;
   private InetAddress endAddress;
   private final int prefixLength;
   String sIP;
   String eIP;
   String routeTempVariable = "4";



   public TestIpRange(String cidr) throws UnknownHostException {

      this.cidr = cidr;

        /* split CIDR to address and prefix part */
      if (this.cidr.contains("/")) {
         int index = this.cidr.indexOf("/");
         String addressPart = this.cidr.substring(0, index);
         String networkPart = this.cidr.substring(index + 1);

         inetAddress = InetAddress.getByName(addressPart);
         prefixLength = Integer.parseInt(networkPart);

         calculate();
      } else {
         throw new IllegalArgumentException("not an valid CIDR format!");
      }
   }


   private void calculate() throws UnknownHostException {

      ByteBuffer maskBuffer;
      int targetSize;
      if (inetAddress.getAddress().length == 4) {
         maskBuffer =
                 ByteBuffer
                         .allocate(4)
                         .putInt(-1);
         targetSize = 4;
      } else {
         maskBuffer = ByteBuffer.allocate(16)
                 .putLong(-1L)
                 .putLong(-1L);
         targetSize = 16;
      }

      BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);

      ByteBuffer buffer = ByteBuffer.wrap(inetAddress.getAddress());
      BigInteger ipVal = new BigInteger(1, buffer.array());

      BigInteger startIp = ipVal.and(mask);
      BigInteger endIp = startIp.add(mask.not());

      byte[] startIpArr = toBytes(startIp.toByteArray(), targetSize);
      byte[] endIpArr = toBytes(endIp.toByteArray(), targetSize);

      this.startAddress = InetAddress.getByAddress(startIpArr);
      this.endAddress = InetAddress.getByAddress(endIpArr);

//      sIP = String.valueOf(startIp);
//      eIP = String.valueOf(endIp);

      sIP = String.valueOf(startAddress);
      eIP = String.valueOf(endAddress);
      addIpRangesWithMaskRoute(sIP, eIP, String.valueOf(prefixLength), routeTempVariable, cidr);

      for (int i = 0; i < storage.size(); i++) {
         System.out.println(Arrays.deepToString(storage.get(i)));
      }
   }

   private byte[] toBytes(byte[] array, int targetSize) {
      int counter = 0;
      List<Byte> newArr = new ArrayList<Byte>();
      while (counter < targetSize && (array.length - 1 - counter >= 0)) {
         newArr.add(0, array[array.length - 1 - counter]);
         counter++;
      }

      int size = newArr.size();
      for (int i = 0; i < (targetSize - size); i++) {

         newArr.add(0, (byte) 0);
      }

      byte[] ret = new byte[newArr.size()];
      for (int i = 0; i < newArr.size(); i++) {
         ret[i] = newArr.get(i);
      }
      return ret;
   }

   public String getNetworkAddress() {

      return this.startAddress.getHostAddress();
   }

   public String getBroadcastAddress() {
      return this.endAddress.getHostAddress();
   }

   public boolean isInRange(String ipAddress) throws UnknownHostException {
      InetAddress address = InetAddress.getByName(ipAddress);
      BigInteger start = new BigInteger(1, this.startAddress.getAddress());
      BigInteger end = new BigInteger(1, this.endAddress.getAddress());
      BigInteger target = new BigInteger(1, address.getAddress());

      int ipStartingRange = start.compareTo(target);
      int ipEndingRange = target.compareTo(end);

      return (ipStartingRange == -1 || ipStartingRange == 0) && (ipEndingRange == -1 || ipEndingRange == 0);
   }

   public ArrayList<String[]> addIpRangesWithMaskRoute(String startIP, String endIP, String mask, String route, String cidr) {
      String[] IPAddress = new String[5];
      //ArrayList<String[]> temp = new ArrayList<>();

      IPAddress[0] = startIP;
      IPAddress[1] = endIP;
      IPAddress[2] = mask;
      IPAddress[3] = route;
      IPAddress[4] = cidr;
      //ArrayList<String[]> storage = new ArrayList<>();
      //temp.add(IPAddress);
      storage.add(IPAddress);

      return storage;
   }

   public String convertIpToDecimal(String ip) {
      String ipAddress = "192.168.1.10";
      String[] addrArray = ipAddress.split("\\.");

      long ipDecimal = 0;

      for (int i = 0; i < addrArray.length; i++) {

         int power = 3 - i;
         ipDecimal += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
      }

      System.out.println(ipDecimal);
      return String.valueOf(ipDecimal);
   }

   public static void main(String[] args) throws UnknownHostException {
      TestIpRange test = new TestIpRange("192.168.0.0/17");

      System.out.println(test.isInRange("192.168.2.1"));
      test.convertIpToDecimal("192.168.2.0");
      //test.comparedIp("192.168.2.0");
   }
}