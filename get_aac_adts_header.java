// 参数一定要设置正确
// 《音视频开发进阶指南：基于Android和IOS平台的实践》一书中给出的ADTS头的前两个字节是0xFF 0xF9，对照ADTS的WiKi描述（文后有链接），
// 发现第二个字节0xF9（1111 1001）的第5bit为1代表的是 MPEG-2（MPEG Version: 0 for MPEG-4, 1 for MPEG-2）
// 并不是我期望的MPEG-4，故将0xF9改为0xF1(1111 0001)

// 注意：packetLen是ADTS头的长度加上AAC音频数据长度的总和
private byte[] getADTSHeader(int packetLen) {
        byte[] packet = new byte[7];
        int profile = 2;  //AAC LC
        int freqIdx = 11;  //8.0KHz
        int chanCfg = 1;  //CPE 声道数
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
        return packet;
    }
    
    //参考链接：https://wiki.multimedia.cx/index.php/ADTS
