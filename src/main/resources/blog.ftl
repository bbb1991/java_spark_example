<#import "base.ftl" as layout>

    <@layout.main "Login">
<#if username??>
    Welcome ${username} <a href="/logout">Выйти <strike>за границы сознания</strike></a> | <a href="/newpost">Новый пост</a>

    <p>
</#if>


<#list myposts as post>
    <h2><a href="/post/${post["permalink"]}">${post["title"]}</a></h2>
    Posted ${post["date"]?datetime} <i>By ${post["author"]}</i><br>
    Comments:
    <#if post["comments"]??>
        <#assign numComments = post["comments"]?size>
            <#else>
                <#assign numComments = 0>
    </#if>

    <a href="/post/${post["permalink"]}">${numComments}</a>
    <hr>
    ${post["body"]!""}
    <p>

    <p>
        <em>Тэги</em>:
        <#if post["tags"]??>
            <#list post["tags"] as tag>
                ${tag}
            </#list>
        </#if>

    <p>
</#list>
</@layout.main>