			// 编码器那边会先发sps和pps来，头一帧就由sps和pps组成
			int spsLength = bytesToInt(mClient.readLength());
			byte[] sps = mClient.readSPSPPS(spsLength);
			mSps = Arrays.copyOfRange(sps, 4, spsLength);
			for (byte b:mSps) {
				Log.d("SPS Length:",spsLength+"//"+b+"");
			}

			int ppsLength = bytesToInt(mClient.readLength());
			byte[] pps = mClient.readSPSPPS(ppsLength);
			mPps = Arrays.copyOfRange(pps, 4, ppsLength);
			for (byte b:mPps) {
				Log.d("PPS Length:",ppsLength+"//" + b+"");
			}
      
      
      
      format.setByteBuffer("csd-0", ByteBuffer.wrap(mSps));
			format.setByteBuffer("csd-1", ByteBuffer.wrap(mPps));
