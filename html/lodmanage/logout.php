<?php
header("HTTP/1.0 401 Unauthorized");
?>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="refresh" content="0; url=../start.html" />
<title>Logout</title>
</head>
<body>
<script>
try {
   document.execCommand("ClearAuthenticationCache");
}
catch (e) { }

</script>

ログアウトしました。
<a href="../start.html" >トップページへ</a>
</body>
</html>
