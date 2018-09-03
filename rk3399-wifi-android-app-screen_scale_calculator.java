// 缩放比例计算，是等比缩放，所以normalToBigRatioX == normalToBigRatioY ,normalToSmallRationX == normalToSmallRationY

// when normal, it's width is screenWidth/2; when big it's width is (4*screenHeight)/3;
// so the normalToBigRatioX = ((4*screenHeight)/3) / (screenWidth/2) = (8*screenHeight) / (3*screenWidth)
normalToBigRatioX =  (8f* screenHeight) / (3f*screenWidth);

// when normal, it's height is screenWidth*3/8; when big it's height is screenHeight;
// so the normalToBigRatioY = screenHeight / (screenWidth*3/8) = (8*screenHeight) / (3*screenWidth)
normalToBigRatioY = (8f*screenHeight) / (3f*screenWidth);

// when normal, it's width is screenWidth/2; when small it's width is screenWidth - (4*screenHeight)/3;
// so the normalToSmallRationX = (screenWidth/2) / (screenWidth - ((4*screenHeight)/3))
normalToSmallRationX = (screenWidth/2f) / (screenWidth - (4f*screenHeight)/3f);

// when normal, it's height is screenWidth*3/8; when small it's height is 4/3 * (screenWidth - (4*screenHeight)/3);
// so the normalToSmallRationY = (screenWidth*3/8) / (4/3 * (screenWidth - (4*screenHeight)/3))
normalToSmallRationX = (3f*screenWidth/8f) / (3f*screenWidth/4f - screenHeight);
