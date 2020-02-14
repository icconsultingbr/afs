/*
 * MojoMagnify 0.1.1 - JavaScript Image Magnifier
 * Copyright (c) 2008 Jacob Seidelin, cupboy@gmail.com, http://blog.nihilogic.dk/
 * MIT License [http://www.opensource.org/licenses/mit-license.php]
 */


var MojoMagnify = (function() {

	var $ = function(id) {return document.getElementById(id);};
	var dc = function(tag) {return document.createElement(tag);};

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

	function makeMagnifiable(oImg, strZoomSrc) {

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
		oZoom.className = "mojomagnify_zoom";

		var oZoomImg = dc("img");
		oZoomImg.className = "mojomagnify_img";
		oZoomImg.style.position = "absolute";

		var oParent = oImg.parentNode;
		oZoom.appendChild(oZoomImg);

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

		var oZoomBorder = dc("div");
		oZoomBorder.className = "mojomagnify_border";
		oZoom.appendChild(oZoomBorder);

		var oZoomInput = oParent;

		addEvent(oZoomImg, "load", function() {

			addEvent(oCtr, "mouseout", 
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
					}
				}
			);

			addEvent(oZoomInput, "mousemove", 
				function(e) {
					oCtr.style.display = "block";

					var oPos = getEventMousePos(oZoomInput, e);

					if (e.srcElement && isIE()) {
						if (e.srcElement == oZoom) return;
						if (e.srcElement != oZoomInput) {
							var oZoomImgPos = getElementPos(e.srcElement);
							var oImgPos = getElementPos(oImg);
							oPos.x -= (oImgPos.x - oZoomImgPos.x);
							oPos.y -= (oImgPos.y - oZoomImgPos.y);
						}
					}

					var x = e.clientX - (getElementPos(oImg).x - (document.body.scrollLeft||document.documentElement.scrollLeft));
					var y = e.clientY - (getElementPos(oImg).y - (document.body.scrollTop||document.documentElement.scrollTop));

					var iMaskWidth = oZoom.offsetWidth;
					var iMaskHeight = oZoom.offsetHeight;

					var iLeft = oPos.x - iMaskWidth/2;
					var iTop = oPos.y - iMaskHeight/2;

					oZoom.style.left = iLeft + "px";
					oZoom.style.top = iTop + "px";

					var fZoomXRatio = oZoomImg.offsetWidth / iWidth;
					var fZoomYRatio = oZoomImg.offsetHeight / iHeight;

					var iZoomX = Math.round(x * fZoomXRatio);
					var iZoomY = Math.round(y * fZoomYRatio);

					oZoomImg.style.left = -iZoomX + iMaskWidth/2 + "px";
					oZoomImg.style.top = -iZoomY + iMaskWidth/2 + "px";
				}
			);
		});

		oZoomImg.src = strZoomSrc;

	}

	function init() {
		var aImages = document.getElementsByTagName("img");
		var aImg = [];
		for (var i=0;i<aImages.length;i++) {
			aImg.push(aImages[i]);
		}
		for (var i=0;i<aImg.length;i++) {
			var oImg = aImg[i];
			var strZoomSrc = oImg.getAttribute("data-magnifysrc");

			if (strZoomSrc) {
				makeMagnifiable(oImg, strZoomSrc);
			}
		}
	}


	return {
		addEvent : addEvent,
		init : init,
		makeMagnifiable : makeMagnifiable
	};

})();

MojoMagnify.addEvent(window, "load", MojoMagnify.init);
