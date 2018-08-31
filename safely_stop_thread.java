Thread thread = new Thread(new Runnable() {
			public void run() {
				while(loop && Thread.currentThread().isInterrupted()){
					// TODO: do something
				}
			}
		});
		
		thread.start();
		
    // loop = false; 和 thread.interrupt(); 每次只能使用其一，否则会产生InterruptedException
    
		// stop thread
		loop = false;
		
		// stop thread
		thread.interrupt();
    
    
