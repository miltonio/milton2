$(document).ready(function(){
	initUploads();
});

function initUploads() {
	var button = $('#doUpload');
	
	new AjaxUpload(button,{
		action: '_DAV/PUT',
		name: 'picd',
		autoSubmit: true,
		responseType: 'json',
		onSubmit : function(file, ext){
			if( !userUrl ) {
				alert('Please login to upload photos');
				return;
			}
			$("span", button).text('Upload...');
			this.disable();
		},
		onComplete: function(file, response){
			$("span", button).text('Upload again');
			this.enable();
			for( i=0; i<response.length; i++ ) {
				var file = response[i];
			//      	     $("#cvHidden").attr("value",file.href);
			//      	     $("#cvLink").html(file.originalName + "(" + file.length + ")");
			//      	     $("#cvLink").attr("href",file.href);
			}
		}
          
	});
}

function showUploadModal() {
	$("#uploads").dialog({
		modal: true,
		width: 600,
		title: "Upload"
	});
}

(function($){
	$.fn.dragUploadable = function(postURL, fieldName, options) {

		var defaults = {
			dragenterClass: "",
			dragleaveClass: "",
			dropListing: "#dropListing",
			loaderIndicator: "#fileDropContainer .progress"
		};
		var options = $.extend(defaults, options);

		return this.each(function() {
			obj = $(this);
			obj.bind("dragenter", function(event){
				obj.removeClass(options.dragleaveClass);
				obj.addClass(options.dragenterClass);
				event.stopPropagation();
				event.preventDefault();
			}, false);
			obj.bind("dragover", function(event){
				event.stopPropagation();
				event.preventDefault();
			}, false);
			obj.bind("dragleave", function(event){
				obj.removeClass(options.dragenterClass);
				obj.addClass(options.dragleaveClass);
				event.stopPropagation();
				event.preventDefault();
			}, false);
			obj.bind("drop", function(event){
				var data = event.originalEvent.dataTransfer;
				event.stopPropagation();
				event.preventDefault();
				addToList(event.originalEvent.dataTransfer, options.dropListing);
				upload(postURL, fieldName, data, options.loaderIndicator);
			}, false);
		});
	};
})(jQuery);

function dropSetup() {
	var dropContainer = document.getElementById("output");

	dropContainer.addEventListener("dragenter", function(event){
		dropContainer.innerHTML = 'DROP';
		event.stopPropagation();
		event.preventDefault();
	}, false);
	dropContainer.addEventListener("dragover", function(event){
		event.stopPropagation();
		event.preventDefault();
	}, false);
	dropContainer.addEventListener("drop", upload, false);
};

function upload(postURL, fieldName, dataTransfer, loaderIndicator) {
	if( dataTransfer.files ) {
		log("upload", postURL, fieldName, dataTransfer.files.length);
		$.each(dataTransfer.files, function ( i, file ) {
			log("send file", file.fileName);
			var xhr    = new XMLHttpRequest();
			var fileUpload = xhr.upload;
			fileUpload.addEventListener("progress", function(event) {
				if (event.lengthComputable) {
					var percentage = Math.round((event.loaded * 100) / event.total);				
					if (percentage < 100 && loaderIndicator) {
						log("percent complete", percentage);
						$(loaderIndicator).show();
						$(loaderIndicator).css("width", percentage + "%");
						$(loaderIndicator).text(percentage + "%");
					}
				}
			}, false);
				
			fileUpload.addEventListener("load", function(event) {
				$(loaderIndicator).text("Finished");
			}, false);
				
			fileUpload.addEventListener("error", function(event) {
				$(loaderIndicator).text("Error");
			}
			, false);
			xhr.open('PUT', postURL + "/" + file.fileName, true);
			xhr.setRequestHeader('X-Filename', file.fileName);
 
			xhr.send(file);
		});
	} else {
		log("upload: no files to upload");
	}
}

function addToList(dataTransfer, dropListing) {	
	log("addToList");
	var files = dataTransfer.files;
	if( files ) {
		for (i = 0; i < files.length; i++) {
			var f = files[i];
			var reader = new FileReader();
			reader.onload = (function(theFile) {
				return function(e) {
					var li = $("<li>");
					var img = $("<img>");
					$(dropListing).append(li);
					li.append(img);
	
					var data = e.target.result;
					img.attr("src", data); // base64 encoded string of local file(s)
					img.attr("width", 150);
					img.attr("height", 150);		
					log("done addToList");	
				};
			})(f);
			reader.readAsDataURL(f);
		}	
	} else {
		log("no files droppped? must be IE...");
	}
}
