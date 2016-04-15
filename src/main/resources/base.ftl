<#macro head_param title>
    <!DOCTYPE html>
    <html>
    <head>
        <title>${title}</title>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1">
    <body>
    </head>
</#macro>

<#-- TODO Need to read about overriding default params -->

<#macro foreign_files>
    <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
    <script src="https://code.jquery.com/jquery-2.2.3.min.js"></script>
    <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
</#macro>
<#macro footer_param>
    </body>
    </html>
</#macro>