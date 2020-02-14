/*
 * MojoZoom 0.1 - JavaScript Image Zoom
 * Copyright (c) 2008 Jacob Seidelin, cupboy@gmail.com, http://blog.nihilogic.dk/
 * MIT License [http://www.opensource.org/licenses/mit-license.php]
 */


var MojoZoom = (function() {

	var $ = function(id) {return document.getElementById(id);};
	var dc = function(tag) {return document.createElement(tag);};

	var iDefaultWidth = 500;
	var iDefaultHeight = 500;

	function addEvent(oElement, strEvent, fncHandler) 
	{
		var fncEvent = function(e) {
			return fncHandler(e||window.event);
		}
		if (oElement.addEventListener) { 
			oElement.addEventListener(strEvent, fncEvent, false); 
		} else if (oElement.attachEvent) { 
			oElement.attachEvent("on" + strEvent, fncEvent); 
		}
	}

	function getElementPos(oElement)
	{
		var x = oElement.offsetLeft;
		var y = oElement.offsetTop;
		var oParent = oElement.offsetParent;
		while (oParent) {
			x += oParent.offsetLeft;
			y += oParent.offsetTop;
			oParent = oParent.offsetParent;
		}
		return {
			x : x,
			y : y
		}
	}

	function getEventMousePos(oElement, e) {
		var iScrollX = document.body.scrollLeft || document.documentElement.scrollLeft;
		var iScrollY = document.body.scrollTop || document.documentElement.scrollTop;

		if (e.currentTarget) {
			var oElPos = getElementPos(oElement);
			return {
				x : e.clientX - oElPos.x + iScrollX,
				y : e.clientY - oElPos.y + iScrollY
			}

		}

		return {
			x : e.offsetX,
			y : e.offsetY
		}
	}

	function isIE() { // sorry
		return !!document.all && !!window.attachEvent && !window.opera;
	}


	function makeZoomable(oImg, strZoomSrc, oZoomImgCtr, iZWidth, iZHeight) {

		var iWidth = oImg.offsetWidth;
		var iHeight = oImg.offsetHeight;


		var oOldParent = oImg.parentNode;
		if (oOldParent.nodeName != "A") {
			var oLinkParent = dc("a");
			oLinkParent.setAttribute("href", strZoomSrc);
			oOldParent.replaceChild(oLinkParent, oImg);
			oLinkParent.appendChild(oImg);
		} else {
			var oLinkParent = oOldParent;
		}

		oLinkParent.style.position = "relative";
		oLinkParent.style.display = "block";
		oLinkParent.style.width = iWidth+"px";
		oLinkParent.style.height = iHeight+"px";


		var oZoom = dc("div");
		oZoom.className = "mojozoom_marker";

		var oZoomImg = dc("img");
		oZoomImg.className = "mojozoom_img";

		oZoomImg.style.position = "absolute";
		oZoomImg.style.left = "-9999px";
                oZoomImg.style.width = 4*iWidth+"px";
                oZoomImg.style.heigh = 4*iHeight+"px";
		document.body.appendChild(oZoomImg);

		var oParent = oImg.parentNode;

		var oCtr = dc("div");
		oCtr.style.position = "absolute";
		oCtr.style.left = "0px";
		oCtr.style.top = "0px";
		oCtr.style.width = iWidth+"px";
		oCtr.style.height = iHeight+"px";
		oCtr.style.overflow = "hidden";
		oCtr.style.display = "none";

		oCtr.appendChild(oZoom);
		oParent.appendChild(oCtr);

		var oZoomInput = oParent;

		var bDefaultCtr = false;
		if (!oZoomImgCtr) {
			oZoomImgCtr = dc("div");
			oZoomImgCtr.className = "mojozoom_imgctr";

			var oImgPos = getElementPos(oImg);
			oZoomImgCtr.style.left = iWidth + oImgPos.x + "px";
			oZoomImgCtr.style.top = oImgPos.y + "px";

			oZoomImgCtr.style.width = (iZWidth ? iZWidth : iDefaultWidth) +"px";
			oZoomImgCtr.style.height = (iZHeight ? iZHeight : iDefaultHeight) +"px";

			document.body.appendChild(oZoomImgCtr);

			bDefaultCtr = true;
		}

		oZoomImgCtr.style.overflow = "hidden";
		oZoomImgCtr.style.visibility = "hidden";

		addEvent(oZoomImg, "load", function() {
			var iZoomWidth = oZoomImg.offsetWidth;
			var iZoomHeight = oZoomImg.offsetHeight;

			var iCtrWidth = oZoomImgCtr.offsetWidth;
			var iCtrHeight = oZoomImgCtr.offsetHeight;

			var fRatioW = iZoomWidth / iWidth;
			var fRatioH = iZoomHeight / iHeight;

			var iMarkerWidth = Math.round(iCtrWidth / fRatioW);
			var iMarkerHeight = Math.round(iCtrHeight / fRatioH);

			document.body.removeChild(oZoomImg);
			oZoomImgCtr.appendChild(oZoomImg);

			var oZoomFill = dc("div");
			oZoomFill.className = "mojozoom_fill";
			oZoom.appendChild(oZoomFill);

			var oZoomBorder = dc("div");
			oZoomBorder.className = "mojozoom_border";
			oZoom.appendChild(oZoomBorder);

			oZoom.style.width = iMarkerWidth+"px";
			oZoom.style.height = iMarkerHeight+"px";

			addEvent(oZoomInput, "mouseout", 
				function(e) {
					var oTarget = e.target || e.srcElement;
					if (!oTarget) return;
					if (oTarget.nodeName != "DIV") return;
					var oRelated = e.relatedTarget || e.toElement;
					if (!oRelated) return;
					while (oRelated != oTarget && oRelated.nodeName != "BODY" && oRelated.parentNode) {
						oRelated = oRelated.parentNode;
					}
					if (oRelated != oTarget) {
						oCtr.style.display = "none";
						oZoomImgCtr.style.visibility = "hidden";
					}
				}
			);

			addEvent(oZoomInput, "mousemove", 
				function(e) {
					var oImgPos = getElementPos(oImg);

					if (bDefaultCtr) {
						oZoomImgCtr.style.left = iWidth + oImgPos.x + "px";
						oZoomImgCtr.style.top = oImgPos.y + "px";
					}
					oCtr.style.display = "block";
					oZoomImgCtr.style.visibility = "visible";

					var oPos = getEventMousePos(oZoomInput, e);

					if (e.srcElement && isIE()) {
						if (e.srcElement == oZoom) return;
						if (e.srcElement != oZoomInput) {
							var oZoomImgPos = getElementPos(e.srcElement);
							oPos.x -= (oImgPos.x - oZoomImgPos.x);
							oPos.y -= (oImgPos.y - oZoomImgPos.y);
						}
					}

					oPos.x = Math.min(Math.max(oPos.x, iMarkerWidth/2), iWidth-iMarkerWidth/2);
					oPos.y = Math.min(Math.max(oPos.y, iMarkerHeight/2), iHeight-iMarkerHeight/2);

					var iLeft = oPos.x - iMarkerWidth/2;
					var iTop = oPos.y - iMarkerHeight/2;

					oZoom.style.left = iLeft + "px";
					oZoom.style.top = iTop + "px";

					oZoomImg.style.left = -(oPos.x*fRatioW - iCtrWidth/2)+"px";
					oZoomImg.style.top = -(oPos.y*fRatioH - iCtrHeight/2)+"px";

				}
			);
		});

		oZoomImg.src = strZoomSrc;

	}

	function init() {
		var aImages = document.getElementsByTagName("img");
		for (var i=0;i<aImages.length;i++) {

			var oImg = aImages[i];
			var strZoomSrc = oImg.getAttribute("data-zoomsrc");

			if (strZoomSrc) {
				makeZoomable(oImg, strZoomSrc, document.getElementById(oImg.getAttribute("id") + "_zoom"));
			}
		}
	}


	return {
		addEvent : addEvent,
		init : init,
		makeZoomable : makeZoomable
	};

})();

MojoZoom.addEvent(window, "load", MojoZoom.init);
