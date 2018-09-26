<TextView
            android:id="@+id/tvCam1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:clickable="true"
            android:layout_marginLeft="10dp"
            android:background="@drawable/tv_bg_shape"
            android:text="Cam1"
            android:textColor="@android:color/white"
            android:textSize="15sp"
            android:visibility="visible" />


        <TextView
            android:id="@+id/tvCam2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:clickable="true"
            android:layout_marginLeft="10dp"
            android:background="@drawable/tv_bg_shape"
            android:text="Cam2"
            android:textColor="@android:color/white"
            android:textSize="15sp"
            android:visibility="visible" />
            
private TextView tvCam1;
private TextView tvCam2;
    
tvCam1 = findViewById(R.id.tvCam1);
tvCam2 = findViewById(R.id.tvCam2);

tvCam1.setOnClickListener(this);
tvCam2.setOnClickListener(this);


private void setCamCenter(final int layoutID) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(layoutID);

        final EditText etAxisX = dialog.findViewById(R.id.etAxisX);
        final EditText etAxisY = dialog.findViewById(R.id.etAxisY);

        dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String axisX = etAxisX.getText().toString();
                String axisY = etAxisY.getText().toString();

                String center = axisX + "," + axisY;

                if(axisX.isEmpty() ||axisY.isEmpty()){
                    showToast(MainActivity.this,"请输入坐标值");
                    return;
                }

                if(layoutID == R.layout.layout_cam1centerxy_value){
                    mJsonHandle.sendCmd(CV.SET_CAMERA1_CNETER,center);
                }else if(layoutID == R.layout.layout_cam2centerxy_value){
                    mJsonHandle.sendCmd(CV.SET_CAMERA2_CNETER,center);
                }

                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
