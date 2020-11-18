package net.ddns.masterlogick.service;

import net.ddns.masterlogick.UI.ViewManager;
import net.ddns.masterlogick.core.IOManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AcQq extends Service {
    private boolean cancel = false;

    @Override
    public List<String> parsePage(String uri) {
        try {
            String jsonData = "";
            while (true) {
                if (cancel) return null;
                String html = IOManager.sendRequest(uri);
                String[] parts = html.split("<script>");
                if (parts.length != 4) {
                    ViewManager.showMessageDialog("Не удалось получить главную страницу");
                    return null;
                }
                String keyEval = parts[2].substring(0, parts[2].indexOf("</script>")).trim();
                keyEval = keyEval.replaceAll("document\\.children", "true");
                if (keyEval.contains("document")) {
                    continue;
                }
                Context context = Context.enter();
                Scriptable s = context.initSafeStandardObjects();
                String encryptedData = parts[3].substring(parts[3].indexOf('\'') + 1, parts[3].lastIndexOf('\''));
                String code = "window = {\"nonce\":\"\",\"DATA\":\"\"};\n" +
                        keyEval + "\n" +
                        "window[\"DATA\"] = '" + encryptedData + " ';\n" +
                        "function Base() {\n" +
                        "    _keyStr = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\";\n" +
                        "    this.decode = function(c) {\n" +
                        "        var a = \"\",\n" +
                        "            b, d, h, f, g, e = 0;\n" +
                        "        for (c = c.replace(/[^A-Za-z0-9\\+\\/\\=]/g, \"\"); e < c.length;) b = _keyStr.indexOf(c.charAt(e++)), d = _keyStr.indexOf(c.charAt(e++)), f = _keyStr.indexOf(c.charAt(e++)), g = _keyStr.indexOf(c.charAt(e++)), b = b << 2 | d >> 4, d = (d & 15) << 4 | f >> 2, h = (f & 3) << 6 | g, a += String.fromCharCode(b), 64 != f && (a += String.fromCharCode(d)), 64 != g && (a += String.fromCharCode(h));\n" +
                        "        return a = _utf8_decode(a)\n" +
                        "    };\n" +
                        "    _utf8_decode = function(c) {\n" +
                        "        for (var a = \"\", b = 0, d = c1 = c2 = 0; b < c.length;) d = c.charCodeAt(b), 128 > d ? (a += String.fromCharCode(d), b++) : 191 < d && 224 > d ? (c2 = c.charCodeAt(b + 1), a += String.fromCharCode((d & 31) << 6 | c2 & 63), b += 2) : (c2 = c.charCodeAt(b + 1), c3 = c.charCodeAt(b + 2), a += String.fromCharCode((d & 15) << 12 | (c2 & 63) << 6 | c3 & 63), b += 3);\n" +
                        "        return a\n" +
                        "    }\n" +
                        "}\n" +
                        "var B = new Base(),\n" +
                        "    T = window['DA' + 'TA'].split(''),\n" +
                        "    N = window['n' + 'onc' + 'e'],\n" +
                        "    len, locate, str;\n" +
                        "N = N.match(/\\d+[a-zA-Z]+/g);\n" +
                        "len = N.length;\n" +
                        "while (len--) {\n" +
                        "    locate = parseInt(N[len]) & 255;\n" +
                        "    str = N[len].replace(/\\d+/g, '');\n" +
                        "    T.splice(locate, str.length)\n" +
                        "}\n" +
                        "T = T.join('');\n" +
                        "B.decode(T)";
                try {
                    jsonData = (String) context.evaluateString(s, code, "<cmd>", 1, null);
                    break;
                } catch (EvaluatorException ignored) {
                } finally {
                    Context.exit();
                }
            }
            try {
                JSONArray json = (JSONArray) new JSONParser().parse(jsonData.substring(jsonData.indexOf("["), jsonData.indexOf("]") + 1));
                ArrayList<String> list = new ArrayList<>();
                json.forEach(o -> {
                    JSONObject data = (JSONObject) o;
                    list.add((String) data.get("url"));
                });
                return list;
            } catch (ParseException e) {
                e.printStackTrace();
                ViewManager.showMessageDialog("Не удалось скачать главную страницу: " + e.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            ViewManager.showMessageDialog("Не удалось скачать главную страницу: " + e.toString());
        }
        return null;
    }

    public boolean accept(String uri) {
        return URI.create(uri).getHost().equals("ac.qq.com");
    }

    public String getInfo() {
        return "Ac.Qq: ac.qq.com";
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}
