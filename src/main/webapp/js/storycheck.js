function HandleBrowseClick()
{
    var fileinput = document.getElementById("browse");
    fileinput.click();
};

function HandleChange()
{
	var fileinput = document.getElementById("browse");
	var textinput = document.getElementById("filename");
	var index = fileinput.value.lastIndexOf('\\');
	textinput.value = fileinput.value.substring(index+1);
};


$(function() {  
    $("textarea[maxlength]").bind('input propertychange', function() {  
        var maxLength = $(this).attr('maxlength');  
        if ($(this).val().length > maxLength) {  
            $(this).val($(this).val().substring(0, maxLength));  
        }  
    })  
});


