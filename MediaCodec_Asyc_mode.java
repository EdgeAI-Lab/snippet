				decoder.setCallback(new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                        try {
                            videoData = h264Queue.take();
//                        Log.d("decode","take a date");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        ByteBuffer buffer = codec.getInputBuffer(index);
                        if (buffer == null) {
                            Log.i(TAG, "buffer=null");
                            return;
                        }
                        buffer.clear();
                        if (videoData == null) {
                            Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            codec.queueInputBuffer(index, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            buffer.put(videoData, 0, videoData.length);
                            buffer.clear();
                            buffer.limit(videoData.length);
                            codec.queueInputBuffer(index, 0, videoData.length, 0,
                                    MediaCodec.BUFFER_FLAG_KEY_FRAME);
                        }
                    }

                    @Override
                    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

                        if(mSurface.isValid()&& mSurface!=null) {
                            //Render the buffer with the default timestamp
                            codec.releaseOutputBuffer(index, true);
                        }

                    }

                    @Override
                    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                    }

                    @Override
                    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                    }
                });
