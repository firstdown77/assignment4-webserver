<html>
     <head>
          <title>Numbers</title>
     </head>

     <body>
     <p><? out.print(new java.util.Date()); ?></p>
     <h1>Hello <? out.print(params.get("name")); ?>!</h1>
     <h2>The numbers 1 to 10:</h2>
     <ul> <? int i;
	     	 for (i=1; i<=10; ++i) { ?>	     	
     			<li>Number <? out.print(i); ?></li> 
     	  <?}?>
     </ul> 
     </body>
</html>
