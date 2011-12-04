import cv2, cv, numpy

def scaleimage(img, size):
    h, w = img.shape[:2]
    scale = max(h, w) / size
    return cv2.resize(img, (int(w/scale), int(h/scale)), interpolation=cv2.INTER_AREA)

def findcircle(img):
    # Gray scale and blur
    gray=cv2.cvtColor(img, cv.CV_BGR2GRAY)
    blur=cv2.GaussianBlur(gray, (5, 5), 0)
    
    # Get min max radius
    h, w = img.shape[:2]
    minrad=min(h,w)/8
    maxrad=min(h,w)/2
    circles=cv2.HoughCircles(blur, cv.CV_HOUGH_GRADIENT, 2, 100, None, 100, 100, minrad, maxrad)

    return (circles[0][0][0],circles[0][0][1],circles[0][0][2])

def drawcircle(img, circledata):
    cv2.circle(img, (circledata[0], circledata[1]), circledata[2], cv.Scalar(255,0,0),1)

def extractcircle(img, circledata):
    rad=int(circledata[2])-2
    sqr=numpy.zeros((rad*2, rad*2, 3), numpy.uint8)
    mask=numpy.zeros((rad*2, rad*2, 1), numpy.uint8)
    offset=(circledata[0]-rad, circledata[1]-rad)
    for x in xrange(0,int(rad*2)):
        for y in xrange(0,int(rad*2)):
            dist=(x-rad)*(x-rad)+(y-rad)*(y-rad)
            if dist<(rad*rad):
                sqr[y][x]=img[y+offset[1]][x+offset[0]]
                mask[y][x]=255
            
    return sqr, mask

def getmask(rad):
    sqr=numpy.zeros((rad*2, rad*2, 1), numpy.uint8)
    cv2.circle(sqr, (rad, rad), rad, cv.Scalar(255),-1)
    return sqr

def determinebackground(img, mask):
    # Get histogram
    histr=gethistsimple(img, 2)
    histg=gethistsimple(img, 1)
    histb=gethistsimple(img, 0)
    
    return (bestval(histb)[0], bestval(histg)[0], bestval(histr)[0])

def gethist(img, mask, channel):
    h, w = img.shape[:2]
    # Get histogram
    hist=numpy.zeros([256], numpy.uint32)
    # Get max histogram value
    for x in xrange(0,w):
        for y in xrange(0,h):
            if mask[y][x]>0:
                hist[img[y][x][channel]]+=1
    return hist

def gethistsimple(img, channel):
    h, w = img.shape[:2]
    # Get histogram
    hist=numpy.zeros([256], numpy.uint32)
    # Get max histogram value
    for x in xrange(0,w):
        for y in xrange(0,h):
            hist[img[y][x][channel]]+=1
    hist[0]=0
    return hist

def fillbackground(img, mask, color):
    h, w = img.shape[:2]
    for x in xrange(0,w):
        for y in xrange(0,h):
            if mask[y][x]==0:
                img[y][x]=color
    

def gauss(x, a, mu, sigma):
    return a*np.exp(-(x-mu)**2/(2*sigma**2))

def bestval(hist):
    bestval=0
    bestidx=0
    for idx in xrange(0,256):
        if hist[idx]>bestval:
            bestidx=idx
            bestval=hist[idx]
    return bestidx, bestval

def normimage(img, color):
    nrm=numpy.copy(img)
    h, w = img.shape[:2]

    # Get max histogram value
    for x in xrange(0,w):
        for y in xrange(0,h):
            for c in xrange(0,3):
                if img[y][x][c]<color[c]:
                    nrm[y][x][c]=img[y][x][c]*192/color[c]
                else:
                    nrm[y][x][c]=(img[y][x][c]-color[c])*64/(255-color[c])+192
    return nrm
    
def removeyellow(img):
    h, w = img.shape[:2]

    # Get max histogram value
    for x in xrange(0,w):
        for y in xrange(0,h):
            img[y][x][0]=max(min(img[y][x][1],img[y][x][2]),img[y][x][0])

def removeyellow2(img):
    h, w = img.shape[:2]
    noyel=numpy.copy(img)

    # Get max histogram value
    for x in xrange(0,w):
        for y in xrange(0,h):
            # If yellow (B<min(R,G))
            if img[y][x][0]<min(img[y][x][1],img[y][x][2]):
                noyel[y][x]=(192, 192, 192)
    return noyel
            
def filtercolonies(img):
    red=numpy.compress([False, False, True], img, 2)
    red=192-red.clip(0,192)
    
    # Remove green and blue
    green=numpy.compress([False, True, False], img, 2)
    blue=numpy.compress([True, False, False], img, 2)
    greenblue=numpy.minimum(192-green.clip(0,192), 192-blue.clip(0,192))+64
    col=(greenblue-red).clip(64,255)-64
    col=255-((col*3).clip(0,255))
    return col

def filtercolonies2(img):
    red=numpy.compress([False, False, True], img, 2)
    return 255-(192-red.clip(0,192))
    
def filterecoli(img):
    red=numpy.compress([False, False, True], img, 2)
    green=numpy.compress([False, True, False], img, 2)
    blue=numpy.compress([True, False, False], img, 2)

    red=192-red.clip(0,192)+64
    
    # Remove green and blue
    greenblue=numpy.minimum(192-green.clip(0,192), 192-blue.clip(0,192))
    col=(red-greenblue).clip(64,255)-64
    col=255-((col*3).clip(0,255))
    return col
    
def findcolonies(col):
    thrs=cv2.adaptiveThreshold(col, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY_INV, 5, 18)
    cont=cv2.findContours(thrs, cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)
    return cont[0]

def processimage(img):
    img=scaleimage(img, 1024)
    
    # Find circle and show
    img2=numpy.copy(img)
    circledata=findcircle(img)
    drawcircle(img2, circledata)
    cv2.imwrite('stage1.png', img2)
    
    # Extract circle
    sqr,mask=extractcircle(img, circledata)
    cv2.imwrite('stage2.png', sqr)
    
    # Figure out background color
    backcolor=determinebackground(sqr, mask)
    
    # Normalize background
    fillbackground(sqr, mask, backcolor)
    nrm=normimage(sqr,backcolor)
    cv2.imwrite('stage3.png', nrm)
    
    # Remove yellow grid lines
    noyel=removeyellow2(nrm)
    cv2.imwrite('stage4.png', noyel)
    
    # Filter everything but colonies (red dots)
    col=filtercolonies2(noyel)
    cv2.imwrite('stage5colonies.png', col)
    
    # Filter everything but e coli (red dots)
    cv2.imwrite('stage6ecoli.png', filterecoli(noyel))
    
    # Locate colonies
    drawn=numpy.copy(noyel)
    cols=findcolonies(col)
    for col in cols:
        rect=cv2.boundingRect(col)
        cv2.rectangle(drawn, (rect[0]-2, rect[1]-2), (rect[0]+rect[2]+2, rect[1]+rect[3]+2), cv.Scalar(0,0,255))
    cv2.imwrite('stage7cols.png', drawn)
    

# Load images    
img = cv2.imread('IMG_6182.JPG')
processimage(img)

#cv2.imshow('img', nrm)
#cv2.waitKey()
#cv.Canny(gray, edges, 50, 200, 3)
#cv.Smooth(gray, gray, cv.CV_GAUSSIAN, 9, 9)

#storage = cv2.CreateMat(1, 2, cv.CV_32FC3)

#scale = max(h, w) / 512.0
#small = cv2.resize(img, (int(w/scale), int(h/scale)), interpolation=cv2.INTER_AREA)
##cv.Smooth(small, small, cv.CV_GAUSSIAN, 9, 9)
#cv2.GaussianBlur(small, (5, 5), 0)
#gray=cv2.cvtColor(small, cv.CV_BGR2GRAY)
#edges=cv2.Canny(gray, 100, 100)

#circles=None
#circles= cv2.HoughCircles(edges, cv.CV_HOUGH_GRADIENT, 2, 100, circles, 100, 100, 50, 256)

##for i in xrange(0,len(circles[0])):
#i=0
#center=(circles[0][i][0],circles[0][i][1])
#rad=circles[0][i][2]
##cv2.circle(small, center, rad, cv.Scalar(255,0,0),1)

##cv2.imshow('edges', edges)

#roi = numpy.empty_like(small)
#cv2.circle(roi, center, rad, cv.Scalar(255, 255, 255), -1, 8, 0) 
#small=numpy.bitwise_and(roi, small)
 
##circlemat=cv2.at(

