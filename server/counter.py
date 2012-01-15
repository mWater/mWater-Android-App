import cv2, cv, numpy, math

def scaleimage(img, size):
    h, w = img.shape[:2]
    scale = max(h, w) / size
    return cv2.resize(img, (int(w/scale), int(h/scale)), interpolation=cv2.INTER_AREA)

def findcircle(img):
    # Gray scale (green component) and blur
    green=numpy.compress([False, True, False], img, 2)
    blur=cv2.GaussianBlur(green, (3, 3), 0)
    
    # Get min max radius
    h, w = img.shape[:2]
    minrad=min(h,w)/8
    maxrad=min(h,w)/2
    circles=cv2.HoughCircles(blur, cv.CV_HOUGH_GRADIENT, 1, 100, None, 100, 50, minrad, maxrad)

    return (circles[0][0][0],circles[0][0][1],circles[0][0][2])

def drawcircle(img, circledata):
    cv2.circle(img, (circledata[0], circledata[1]), circledata[2], cv.Scalar(255,0,0),1)

def extractcircle(img, circledata):
    rad=int(circledata[2])-2
    sqr=numpy.zeros((rad*2, rad*2, 3), numpy.uint8)
    offset=(circledata[0]-rad, circledata[1]-rad)
    for x in xrange(0,int(rad*2)):
        for y in xrange(0,int(rad*2)):
            dist=(x-rad)*(x-rad)+(y-rad)*(y-rad)
            if dist<(rad*rad):
                sqr[y][x]=img[y+offset[1]][x+offset[0]]
            else:
                dist=math.sqrt(dist)
                # Move back inside circle by reflecting
                factor=(2*rad-dist)/dist
                newx=(x-rad)*factor+rad
                newy=(y-rad)*factor+rad
                sqr[y][x]=img[newy+offset[1]][newx+offset[0]]
    return sqr, getmask(rad)

def getmask(rad):
    sqr=numpy.zeros((rad*2, rad*2, 1), numpy.uint8)
    cv2.circle(sqr, (rad, rad), rad, cv.Scalar(1),-1)
    return sqr

def highpass(img, mask):
    blur=cv2.blur(img, (100,100), None, (-1,-1), cv2.BORDER_REFLECT)
    
    # Divide out blur in float domain
    diff=img.astype(numpy.float32)/blur.astype(numpy.float32)
    
    # Set non-mask to 1
    h, w = diff.shape[:2]
    for x in xrange(0,w):
        for y in xrange(0,h):
            if mask[y][x]==0:
                diff[y][x]=1

    return diff

def findbacteria(col, ecoli, bubbles, drawon):
    bacts=cv2.findContours(col.astype(numpy.uint8), cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)[0]

    # Bacteria neighborhood size 1/50th of dish
    h, w = col.shape[:2]
    neighsize=h/50
    
    numecoli=0
    numtherm=0
    numother=0
    
    for bact in bacts:
        rect=cv2.boundingRect(bact)
        center=(rect[0]+rect[2]/2, rect[1]+rect[3]/2)
        topleft=(max(0, center[0]-neighsize), max(0, center[1]-neighsize))
        bottomright=(min(w-1, center[0]+neighsize), min(h-1, center[1]+neighsize))
        
        ecolicount=numpy.average(ecoli[topleft[1]:bottomright[1]+1, topleft[0]:bottomright[0]+1])
        bubblecount=numpy.average(bubbles[topleft[1]:bottomright[1]+1, topleft[0]:bottomright[0]+1])
        #print "e:{0:0.00} b:{1:0.00}".format(ecolicount, bubblecount)
        #text="e:{0:0.00} b:{1:0.00}".format(ecolicount, bubblecount)
        #cv2.putText(drawon, text, bottomright, cv2.FONT_HERSHEY_SIMPLEX, 0.4, (0,0,0))
        
        # Create outer boundary
        #cv2.rectangle(drawon, (rect[0]-neighsize, rect[1]-neighsize), (rect[0]+rect[2]+neighsize, rect[1]+rect[3]+neighsize), cv.Scalar(0,0,255))
        if ecolicount>0.1:
            color=cv.Scalar(255,0,0)
            numecoli+=1
        elif bubblecount>0.03:
            color=cv.Scalar(0,0,255)
            numtherm+=1
        else:
            color=cv.Scalar(0,0,0)
            numother+=1
        cv2.rectangle(drawon, topleft, bottomright, color)
            
    return numecoli, numtherm, numother


def analyse(img):
    red=numpy.compress([False, False, True], img, 2)
    green=numpy.compress([False, True, False], img, 2)

    col=cv2.threshold(green, 0.75, 1, cv2.THRESH_BINARY_INV)[1]
    ecoli=cv2.threshold(red, 0.9, 1, cv2.THRESH_BINARY_INV)[1]
    bubbles=cv2.threshold(green, 1.05, 1, cv2.THRESH_BINARY)[1]
    
    cv2.imwrite('col.png', col*255)
    cv2.imwrite('ecoli.png', ecoli*255)
    cv2.imwrite('bubbles.png', bubbles*255)
    
    return col, ecoli, bubbles

def createfalsecolor(col, ecoli, bubbles):
    h, w = col.shape[:2]
    img=numpy.zeros((h, w, 3), numpy.uint8)
    for x in xrange(0,w):
        for y in xrange(0,h):
            if col[y][x]>0:
                img[y][x]=(0,0,255)
            elif ecoli[y][x]>0:
                img[y][x]=(255,0,0)
            elif bubbles[y][x]>0:
                img[y][x]=(192,192,192)
            else:
                img[y][x]=(255,255,255)
    return img
    

def processimage(img):
    img=scaleimage(img, 2048)
    cv2.imwrite('stage0.png', img)
    
    # Find circle and show
    img2=numpy.copy(img)
    circledata=findcircle(img)
    drawcircle(img2, circledata)
    cv2.imwrite('stage1.png', img2)
    
    # Extract circle
    sqr,mask=extractcircle(img, circledata)
    cv2.imwrite('stage2.png', sqr)
    
    high=highpass(sqr,mask)
    numpy.save("high", high)
    cv2.imwrite('high.png', high*192)

    col, ecoli, bubbles=analyse(high)

    # Combine into false color image
    falsecolor=createfalsecolor(col, ecoli, bubbles)
    cv2.imwrite('falsecolor.png', falsecolor)
    
    # Locate colonies
    drawon=numpy.copy(sqr*mask)
    numecoli, numtherm, numother = findbacteria(col, ecoli, bubbles, drawon)
    cv2.imwrite('ided.png', drawon)
    return numecoli, numtherm, numother 


# Load images    
img = cv2.imread('IMG_6182.JPG')
numecoli, numtherm, numother = processimage(img)

print "EColi={0} Thermiform={1} Other={2}".format(numecoli, numtherm, numother)
