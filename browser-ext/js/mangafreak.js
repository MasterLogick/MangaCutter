console.log("execution started");
let urls = [];
let query = document.querySelectorAll("img#gohere");
if (query.length === 0) {
    alert("This page is not a chapter");
} else {
    query.forEach(function (item) {
        urls.push(item.src);
    });
    browser.runtime.sendMessage({
        "type": "download-chapter",
        "data": urls
    });
}