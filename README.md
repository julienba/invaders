# invaders

A Clojure library designed to track down invaders.

## Usage

To see the invaders (or potential invaders), start a REPL with `lein repl` and run the function `(demo)`

To run it with specific file:
```bash
lein run path-to-radar path-to-invader
```

## TODO
- [x] Test for the str->grid
- [x] Handle empty invader: an invader sould contain at least one 'o'
- [x] More corner case: empty radar, empty invader
- [ ] Note about performance
- [ ] Add screenshot

## Code style

For naming I use the one describe in [Elements of Clojure by Zachary Tellman](https://elementsofclojure.com/). An excellent read if you don't know it yet.

In addition to that:

1. I used private function for function that have no business being used of the current namespace. I still test this function with `'#`. I find this convention useful to refactor.

2. In my test file I use the alias `sut` for [System under test](https://en.wikipedia.org/wiki/System_under_test)

While I like these conventions, I'm not too attached to them either.

## License

Copyright © 2025 Julien Bille
