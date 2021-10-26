<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Unencrypted File Upload</title>
<link rel='stylesheet' href='style.css'  type="text/css" >
<title>PDM crypto</title>
</head>
<body>
    <h2>${requestScope.message}</h2>
    <main>
    
    
        <center>
            <h1>Unencrypted File Upload</h1>
            <form method="post" action="Upload" enctype="multipart/form-data">
                Select file to upload: <input type="file" name="file" /><br />
                <br /> <input type="submit" value="Upload" />
            </form>
        </center>
    
    
    </main>
</body>

<footer>
  <a href="https://github.com/2042third" target="_blank">Source</a>
  <br><br>
  <span id="version"></span><br>
  Copyright (c) 2021 Yang Yi<br>
  <a href="https://dryangyi.com" target="_blank">dryangyi.com</a>
</footer>
</html>