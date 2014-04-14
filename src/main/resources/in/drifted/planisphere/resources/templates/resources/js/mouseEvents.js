var svg;
var angle = 0;
var angle0 = 0;
var angle1 = 0;
var moving = false;
var active, passive, wheel, sizeX, sizeY, isFirefox;

function startup(evt) {
    var userAgent = navigator.userAgent.toLowerCase();
    isFirefox = /firefox/.test(userAgent);
    svg = evt.target.ownerDocument.documentElement;
    active = svg.getElementById("active");
    passive = svg.getElementById("passive");
    wheel = svg.getElementById("wheel");
    sizeX = screen.width;
    sizeY = screen.height;
}

function wheelMouseDown(evt) {
    moving = true;
    if (isFirefox) {
        angle0 = Math.atan2(2 * getY(evt) - sizeY, 2 * getX(evt) - sizeX);
    } else {
        angle0 = Math.atan2(2 * evt.clientY - svg.clientHeight, 2 * evt.clientX - svg.clientWidth);
    }
    passive.setAttribute('style', "cursor: url(../closedhand.cur), crosshair;");
    active.setAttribute('style', "cursor: url(../closedhand.cur), crosshair;");
}

function wheelMouseMove(evt) {
    if (moving) {
        if (isFirefox) {
            angle1 = Math.atan2(2 * getY(evt) - sizeY, 2 * getX(evt) - sizeX);
        } else {
            angle1 = Math.atan2(2 * evt.clientY - svg.clientHeight, 2 * evt.clientX - svg.clientWidth);
        }
        angle = angle + angle1 - angle0;
        angle0 = angle1;
        wheel.setAttribute("transform", "rotate(" + (180 * angle / Math.PI) + ")");
    }
}

function wheelMouseUp(evt) {
    moving = false;
    passive.removeAttribute('style');
    active.setAttribute('style', "cursor: url(../openhand.cur), pointer;");
}

function getX(evt) {
    var posx = 0;
    if (evt.pageX) {
        posx = evt.pageX;
    }
    else if (evt.clientX) {
        posx = evt.clientX + document.body.scrollLeft
                + document.documentElement.scrollLeft;
    }
    return posx;
}

function getY(evt) {
    var posy = 0;
    if (evt.pageY) {
        posy = evt.pageY;
    }
    else if (evt.clientY) {
        posy = evt.clientY + document.body.scrollTop
                + document.documentElement.scrollTop;
    }
    return posy;
}

function synchronize(evt) {
    // security issue in Chrome on local files so 'objects' is not a global variable
    var objects = parent.document.getElementsByTagName("object");
    if (objects.length > 1) {
        for (var i = 0; i < objects.length; i++) {
            var currentWindow = objects[i].getSVGDocument().defaultView;
            var currentId = objects[i].getAttribute("id");
            if ("angle" in currentWindow && currentId !== "planisphere-0") {
                currentWindow.angle = -angle;
                currentWindow.wheel.setAttribute("transform", "rotate(" + (-180 * angle / Math.PI) + ")");
                objects[i].setAttribute("style", "position:absolute; z-index:1");
                objects[i].setAttribute("id", "planisphere-0");
                var otherIndex = (i + 1) % 2;
                objects[otherIndex].setAttribute("style", "position:absolute; z-index:0");
                objects[otherIndex].setAttribute("id", "planisphere-1");
                break;
            }
        }
    }
}