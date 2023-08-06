// Code goes here
/* Fix to Open CTAs in New Tab -- Remove when Alpha fixes sitecore */

$( document ).ready(function() {
     jQuery(".cta").attr("target", "_blank");
  });

/* Mobile Menu */
$( document ).ready(function() {
   jQuery('.menu-title-bar').on('click', function() {
      jQuery('nav .navigation').slideToggle();
    });
   jQuery('.navigation>li').on('click', function() {
      jQuery(this).find('.mega-dd').first().slideToggle();
      jQuery(this).toggleClass('icon');
    });
    jQuery('.navigation a').on('click', function(e){
      e.stopPropagation(); 
    });
});

//var closeBtn = document.getElementById('btnclose');

function ShowModalPopup(id, title, body, showOnce){
    
    if (id == getCookie("BTpopup")) 
        return;
    
    //for close button 
    var popup = $("#popup").clone();
  
    $(popup).find("#popuptitle").html(title);
    //    $("#popuptitle").html(title);
    $(popup).find("#popupcontent").html(body);
    //$("#popupcontent").html(body);

    $(popup).attr("data-popupid", id);

    $(popup).find(".pop-up-close").click(function() {
        $(popup).hide();
        $(popup).remove();
    });
    $(popup).find("#btnclose").click(function() {
        $(popup).hide();
        $(popup).remove();
    });

    $(popup).insertAfter("#popup");
   
    setCookie("BTpopup", id, 365, showOnce);
    $(popup).show();

}


function setCookie(cname, cvalue, exdays, persistent) {
    var d = new Date();
    var expires = '';
    if (persistent) {
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        expires = "expires=" + d.toUTCString();        
    }
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}

function checkCookie() {
    var user = getCookie("BTpopup");
    if (user != "") {
       // alert("Welcome again " + user);
    } else {
       // user = prompt("Please enter your name:", "");
        if (user != "" && user != null) {
            setCookie("BTpopup", user, 365);
        }
    }
}

function LogDebug(message) {
    if (navigator.appVersion.indexOf("MSIE 10") !== -1) {
        return;
    }
    if (window.console) {
        console.debug(message);
    } else {
        alert(message);
    }
}

function setBrowserCompatiability() {
    if (!window.location.origin) {
        window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
    }
}

function isEmpty(val) {
    return (val === undefined || val == null || val == "null" || val.length <= 0) ? true : false;
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search.toLowerCase());
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function redirect(url) {
    var ua = navigator.userAgent.toLowerCase(),
        isIE = ua.indexOf('msie') !== -1,
        version = parseInt(ua.substr(4, 2), 10);

    // Internet Explorer 8 and lower
    if (isIE && version < 9) {
        var link = document.createElement('a');
        link.href = url;
        document.body.appendChild(link);
        link.click();
    }
        // All other browsers can use the standard window.location.href (they don't lose HTTP_REFERER like IE8 & lower does)
    else {
        window.location.href = url;
    }
}

function LogDebug(message) {
    if (navigator.appVersion.indexOf("MSIE 10") !== -1) {
        return;
    }
    if (window.console) {
        console.debug(message);
    } else {
        alert(message);
    }
}

function setBrowserCompatiability() {
    if (!window.location.origin) {
        window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
    }
}

function isEmpty(val) {
    return (val === undefined || val == null || val == "null" || val.length <= 0) ? true : false;
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search.toLowerCase());
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function redirect(url) {
    var ua = navigator.userAgent.toLowerCase(),
        isIE = ua.indexOf('msie') !== -1,
        version = parseInt(ua.substr(4, 2), 10);

    // Internet Explorer 8 and lower
    if (isIE && version < 9) {
        var link = document.createElement('a');
        link.href = url;
        document.body.appendChild(link);
        link.click();
    }
        // All other browsers can use the standard window.location.href (they don't lose HTTP_REFERER like IE8 & lower does)
    else {
        window.location.href = url;
    }
}


