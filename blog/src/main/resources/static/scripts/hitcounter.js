
 function getCookie(byname)	// return the value by name, not used here
 {byname=byname+"=";
     nlen = byname.length;
     fromN = document.cookie.indexOf(byname)+0;
     if((fromN) != -1)
     {fromN +=nlen
         toN=document.cookie.indexOf(";",fromN)+0;
         if(toN == -1) {toN=document.cookie.length;}
         return unescape(document.cookie.substring(fromN,toN));
     }
     return null;
 }

function parseCookie()   // Division of cookie
{ var cookieList = document.cookie.split("; ");
    // Array for each  cookie in cookieList
    var cookieArray = new Array();
    for (var i = 0; i < cookieList.length; i++) {
        // Division of the pair name-value
        var name = cookieList[i].split("=");
        // Decoding and adding to cookie-array
        cookieArray[unescape(name[0])] = unescape(name[1]);
    }
    return cookieArray;
}  
function setCookie(visits) {
    /* Counter of visits with the Date of the Latest visit and definition of data store for the period of 1 year */
    var expireDate = new Date();
    var today = new Date();
    // Setting the date of expire of the storing time 
    expireDate.setDate(365 + expireDate.getDate());
    // Saving the number of visits
    document.cookie = "visits=" + visits + 
                      "; expires=" + expireDate.toUTCString() + ";";
    // Saving the current date, as time of last visit
    document.cookie = "LastVisit=" + escape(today.toUTCString()) +
                       "; expires=" + expireDate.toUTCString() + ";";
}

if ("" == document.cookie)
{ // Initialization cookie.
    setCookie(1);
    document.write("<H3>Welcome, this is the first time you visit my blog !</H3>");
}
else {
    var cookies = parseCookie();
    // Welcome message, number of visits and increase of number of visits by 1
    document.write("<H5><b>Your visits are - " +
       cookies.visits++ + " ! " + "Last time you visit the blog: " + cookies.LastVisit + ".</b></H5>");
    // Last date visit printing
    //document.write("<H5>Last time you visit the blog: " + cookies.LastVisit + ".</H5>");
    // Cookie renewal:
    setCookie(isNaN(cookies.visits)?1:cookies.visits);
}
