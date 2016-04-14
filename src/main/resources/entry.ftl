<!doctype HTML>
<html
<head>
    <title>
        Blog Post
    </title>
</head>
<body>
<#if username??>
    Welcome ${username} <a href="/logout">Logout</a> | <a href="/newpost">New Post</a>

    <p>
</#if>

<a href="/">Blog Home</a><br><br>

<h2>${post["title"]}</h2>
Posted ${post["date"]?datetime}<i> By ${post["author"]}</i><br>
<hr>
${post["body"]}
<p>
    <em>Filed Under</em>:
    <#if post["tags"]??>
        <#list post["tags"] as tag>
            ${tag}
        </#list>
    </#if>
<p>
    Comments:
<ul>
    <#if post["comments"]??>
        <#assign numComments = post["comments"]?size>
            <#else>
                <#assign numComments = 0>
    </#if>
    <#if (numComments > 0)>
        <#list 0 .. (numComments -1) as i>

                Автор: ${post["comments"][i]["author"]}<br>
            <br>
            ${post["comments"][i]["body"]}<br>
            <hr>
        </#list>
    </#if>
    <h3>Не желаете прокомментировать?</h3>

    <form action="/newcomment" method="POST">
        <input type="hidden" name="permalink", value="${post["permalink"]}">
        ${errors!""}<br>
        <b>Name</b> (required)<br>
        <input type="text" name="commentName" size="60" value="${comments["name"]}"><br>
        <b>Email</b> (optional)<br>
        <input type="text" name="commentEmail" size="60" value="${comments["email"]}"><br>
        <b>Comment</b><br>
        <textarea name="commentBody" cols="60" rows="10">${comments["body"]}</textarea><br>
        <input type="submit" value="Submit">
    </form>
</ul>
</body>
</html>


