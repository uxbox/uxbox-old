var Bacon = {};

jQuery.prototype.fn.asEventStream = function () {};

Bacon.fromEventTarget = function() {};
Bacon.fromPromise = function() {};
Bacon.noMore;
Bacon.more;
Bacon.later = function() {};
Bacon.sequentially = function() {};
Bacon.repeatedly = function() {};
Bacon.fromCallback = function() {};
Bacon.fromNodeCallback = function() {};
Bacon.fromPoll = function() {};
Bacon.fromEventTarget = function() {};
Bacon.interval = function() {};
Bacon.constant = function() {};
Bacon.never = function() {};
Bacon.once = function() {};
Bacon.fromArray = function() {};
Bacon.mergeAll = function() {};
Bacon.zipAsArray = function() {};
Bacon.zipWith = function() {};
Bacon.combineWith = function() {};
Bacon.combineTemplate = function() {};

Bacon.Observable.prototype.onValue = function() {};
Bacon.Observable.prototype.onValues = function() {};
Bacon.Observable.prototype.onError = function() {};
Bacon.Observable.prototype.onEnd = function() {};
Bacon.Observable.prototype.errors = function() {};
Bacon.Observable.prototype.filter = function() {};
Bacon.Observable.prototype.takeWhile = function() {};
Bacon.Observable.prototype.endOnError = function() {};
Bacon.Observable.prototype.take = function() {};
Bacon.Observable.prototype.map = function() {};
Bacon.Observable.prototype.mapError = function() {};
Bacon.Observable.prototype.mapEnd = function() {};
Bacon.Observable.prototype.doAction = function() {};
Bacon.Observable.prototype.takeUntil = function() {};
Bacon.Observable.prototype.skip = function() {};
Bacon.Observable.prototype.skipDuplicates = function() {};
Bacon.Observable.prototype.withStateMachine = function() {};
Bacon.Observable.prototype.scan = function() {};
Bacon.Observable.prototype.fold = function() {};
Bacon.Observable.prototype.zip = function() {};
Bacon.Observable.prototype.diff = function() {};
Bacon.Observable.prototype.flatMap = function() {};
Bacon.Observable.prototype.flatMapFirst = function() {};
Bacon.Observable.prototype.flatMapLatest = function() {};
Bacon.Observable.prototype.not = function() {};
Bacon.Observable.prototype.log = function() {};
Bacon.Observable.prototype.slidingWindow = function() {};
Bacon.Observable.prototype.combine = function() {};
Bacon.Observable.prototype.decode = function() {};
Bacon.Observable.prototype.reduce = function() {};

Bacon.EventStream.prototype.map = function() {};
Bacon.EventStream.prototype.delay = function() {};
Bacon.EventStream.prototype.debounce = function() {};
Bacon.EventStream.prototype.debounceImmediate = function() {};
Bacon.EventStream.prototype.throttle = function() {};
Bacon.EventStream.prototype.bufferWithTime = function() {};
Bacon.EventStream.prototype.bufferWithCount = function() {};
Bacon.EventStream.prototype.bufferWithTimeOrCount = function() {};
Bacon.EventStream.prototype.buffer = function() {};
Bacon.EventStream.prototype.merge = function() {};
Bacon.EventStream.prototype.toProperty = function() {};
Bacon.EventStream.prototype.concat = function() {};
Bacon.EventStream.prototype.skipUntil = function() {};
Bacon.EventStream.prototype.awaiting = function() {};
Bacon.EventStream.prototype.startWith = function() {};
Bacon.EventStream.prototype.withHandler = function() {};
Bacon.EventStream.prototype.withSubscribe = function() {};
Bacon.EventStream.prototype.skipWhile = function() {};
Bacon.EventStream.prototype.sampledBy = function() {};


Bacon.Property.prototype.sample = function() {};
Bacon.Property.prototype.sampledBy = function() {};
Bacon.Property.prototype.changes = function() {};
Bacon.Property.prototype.withHandler = function() {};
Bacon.Property.prototype.withSubscribe = function() {};
Bacon.Property.prototype.toProperty = function() {};
Bacon.Property.prototype.toEventStream = function() {};
Bacon.Property.prototype.and = function() {};
Bacon.Property.prototype.or = function() {};
Bacon.Property.prototype.delay = function() {};
Bacon.Property.prototype.debounce = function() {};
Bacon.Property.prototype.throttle = function() {};
Bacon.Property.prototype.delayChanges = function() {};
Bacon.Property.prototype.startWith = function() {};
Bacon.Property.prototype.awaiting = function() {};

Bacon.Bus = function() {};
Bacon.Bus.prototype.plug = function() {};
Bacon.Bus.prototype.push = function() {};
Bacon.Bus.prototype.end = function() {};
Bacon.Bus.prototype.error = function() {};

Bacon.Initial = function() {};
Bacon.Next = function() {};
Bacon.End = function() {};
Bacon.Error = function() {};
