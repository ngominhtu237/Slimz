'use strict';
function addArrow() {
    var elements = document.getElementsByClassName('arrow');
    for(var i=0; i<elements.length; i++) {
        var newWord = elements[i].innerHTML.replace(/\\"/g, '');
        elements[i].innerHTML = "<span style='color:green; text-decoration: none; '>" + String.fromCharCode(0x27AD) + "</span>" + " " + newWord;
    }
}

function formatTitle() {
    var elements = document.getElementsByClassName('title');
    for(var i=0; i<elements.length; i++) {
        var oldTitle = elements[i].innerHTML;
        var arrayWords = oldTitle.split(" ");
        var newTitle = ' ';
        for(var j=0; j<arrayWords.length; j++) {
            var word = arrayWords[j];
            if(word.startsWith('/')) {
                word = word.replace(":", String.fromCharCode(0x02D0));
                word = "<span class='spell' style='color:black; font-size: 16px; text-decoration: none; font-weight: normal;'>" + word + "</span>";
            } else {
                 word = "<span style=''>" + word + "</span>";
            }
            newTitle = newTitle + word + " ";
        }
        elements[i].innerHTML = newTitle;
    }
}

function deleteColon() {
    var elements = document.getElementsByClassName('delColon');
    for(var i=0; i<elements.length; i++) {
         var rs = elements[i].innerHTML.replace(":", "");
         rs = rs.replace(/\\"/g, '');
         elements[i].innerHTML = rs;
    }
}

function underScore() {
    var elements = document.getElementsByClassName('underscore');
    for(var i=0; i<elements.length; i++) {
            var oldTitle = elements[i].innerHTML;
            // check oldTitle is english
            var regex = /^[A-Za-z0-9]*$/;
            var arrayWords = oldTitle.split(" ");
            var isEng = true;
            for(var k=0; k<arrayWords.length; k++) {
                if (!regex.test(arrayWords[k])) {
                     isEng = false;
                }
            }
            if(isEng) {
                var newTitle = ' ';
                for(var j=0; j<arrayWords.length; j++) {
                    newTitle = newTitle + "<button id='btnOK' value='" + arrayWords[j] +"' onclick='ok.performClick(this.value);'style='background: none;color: inherit;border: none;padding: 0;font: inherit;cursor: pointer; outline: inherit;'><span id='mValue' style='text-decoration: underline;'>" + arrayWords[j] + "</span></button>" + " ";
                }
                elements[i].innerHTML = newTitle;
            }
    }
}
