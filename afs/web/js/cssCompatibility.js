var cssCompatibility=function(){var B=navigator.userAgent.toLowerCase(),D=function(G){return B.indexOf(G)!=-1},C=document.getElementsByTagName("html")[0],A=(!(/opera|webtv/i.test(B))&&/msie (\d)/.test(B))?("ie ie"+RegExp.$1):D("gecko/")?"gecko":D("opera/9")?"opera opera9":/opera (\d)/.test(B)?"opera opera"+RegExp.$1:D("konqueror")?"konqueror":D("applewebkit/")?"webkit safari":D("mozilla/")?"gecko":"",E=(D("x11")||D("linux"))?" linux":D("mac")?" mac":D("win")?" win":"";var F=A+E+" js";C.className+=C.className?" "+F:F}();