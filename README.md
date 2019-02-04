# Comments persister

This application is consuming `n` posts of `https://jsonplaceholder.typicode.com/` API, 
then grouping them by `mail domain`, and saving into catalogs on disk with name such as `mail domain`.

For example, executing app with argument `2` should:
1. Get comments from:
```
https://jsonplaceholder.typicode.com/comments?postId=1
https://jsonplaceholder.typicode.com/comments?postId=2
```
2. Create certain catalogs:
```
- ./biz
- ./com
- ./tv
- ./me
- ./org
- ./us
- ./name
``` 
3. All catalogs should contain file `comments.txt` containing collected comments.
