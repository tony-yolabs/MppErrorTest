@testable import shared
import XCTest

class FlowTests: XCTestCase {

    // nothing thrown
    func testNoThrowSimple() {
        XCTAssertNoThrow(FlowErrorEmitter().noThrowSimple())
    }

    // thrown in flow, not exposed (launch block)
    func testNoThrowLaunch() {
        XCTAssertNoThrow(FlowErrorEmitter().noThrowLaunch())
    }

    // thrown in flow, exposed via suspend
    // - upstream method annotated with @Throws
    // - no `try` in call
    // - returned as cast to NSError
    func testThrowLaunchSuspend() {
        let expectation = XCTestExpectation(description: "testNoThrowExceptionSuspend")
        XCTAssertNoThrow(
            FlowErrorEmitter().throwLaunchSuspend() { _, error in
                XCTAssert((error as Any) is NSError, "Returned error is not NSError")
                let kException = (error! as NSError).kotlinException
                XCTAssert(kException is FlowErrorEmitterException, "Returned exception is not FlowErrorCustomException")
                expectation.fulfill()
            }
        )

        wait(for: [expectation], timeout: 5.0)
    }

    // thrown in flow, not exposed (launch block)
    // - rethrown before terminal flow
    func testNoThrowLaunchRethrowCollect() {
        XCTAssertNoThrow(FlowErrorEmitter().noThrowLaunchRethrowCollect())
    }

    // thrown in flow, with handler, not exposed (launch block)
    // - rethrown before terminal flow
    func testNoThrowLaunchRethrowCollectWithHandler() {
        XCTAssertNoThrow(FlowErrorEmitter().noThrowLaunchRethrowCollectWithHandler())
    }

    // thrown in flow, not exposed (launch block)
    // - rethrown after terminal flow
    func testNoThrowLaunchRethrowOnEach() {
        XCTAssertNoThrow(FlowErrorEmitter().noThrowLaunchRethrowOnEach())
    }

    // thrown in flow, with handler, not exposed (launch block)
    // - rethrown after terminal flow
    func testnNoThrowLaunchRethrowOnEachWithHandler() {
        XCTAssertNoThrow(FlowErrorEmitter().noThrowLaunchRethrowOnEachWithHandler())
    }


    // thrown in flow, with handler, exposed via suspend
    // - rethrown after terminal flow
    func testThrowSuspendRethrowOnEachWithHandler() {
        let expectation = XCTestExpectation(description: "throwSuspendRethrowOnEachWithHandler")

        XCTAssertNoThrow(
            FlowErrorEmitter().throwSuspendRethrowOnEachWithHandler() { _, error in
                XCTAssert((error as Any) is NSError, "Returned error is not NSError")
                let kException = (error! as NSError).kotlinException
                XCTAssert(kException is FlowErrorEmitterException, "Returned exception is not FlowErrorEmitterException")
                expectation.fulfill()
            }
        )

        wait(for: [expectation], timeout: 5.0)
    }


    // exception *is* thrown before coroutine and exposed to client
    func testNoThrowFlowBeforeLaunch() {
        let expectation = XCTestExpectation(description: "testNoThrowFlowBeforeLaunch")

        XCTAssertThrowsError(try FlowErrorCollector().throwExceptionBeforeLaunch()) { error in
            let kException = (error as NSError).kotlinException
            XCTAssert(kException is FlowErrorCollectorException, "Returned exception is not FlowErrorEmitterException")
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    // no exception exposed - thrown in coroutine launch loop
    func testNoThrowFlowBefore() {
        XCTAssertNoThrow(FlowErrorCollector().noThrowExceptionBeforeCollect())
    }

    // no exception exposed - thrown in coroutine launch loop
    func testNoThrowFlowDuring() {
        XCTAssertNoThrow(FlowErrorCollector().noThrowExceptionDuringCollect())
    }

    // no exception exposed - thrown in coroutine launch loop
    func testNoThrowFlowAfter() {
        XCTAssertNoThrow(FlowErrorCollector().noThrowExceptionAfterCollect())
    }

    // exception *is* thrown before coroutine and exposed to client via error callback
    func testThrowExceptionBeforeCollectSuspend() {
        let expectation = XCTestExpectation(description: "throwExceptionBeforeCollectSuspend")
        XCTAssertNoThrow(
            FlowErrorCollector().throwExceptionBeforeCollectSuspend() { _, error in
                XCTAssert((error as Any) is NSError, "Returned error is not NSError")
                let kException = (error! as NSError).kotlinException
                XCTAssert(kException is FlowErrorCollectorException, "Returned exception is not FlowErrorEmitterException")
                expectation.fulfill()
            }
        )

        wait(for: [expectation], timeout: 5.0)
    }

    // exception *is* thrown before coroutine and exposed to client via error callback
    func testThrowExceptionDuringCollectSuspend() {
        let expectation = XCTestExpectation(description: "throwExceptionDuringCollectSuspend")
        XCTAssertNoThrow(
            FlowErrorCollector().throwExceptionDuringCollectSuspend() { _, error in
                XCTAssert((error as Any) is NSError, "Returned error is not NSError")
                let kException = (error! as NSError).kotlinException
                XCTAssert(kException is FlowErrorCollectorException, "Returned exception is not FlowErrorEmitterException")
                expectation.fulfill()
            }
        )

        wait(for: [expectation], timeout: 5.0)
    }

    // exception *is* thrown before coroutine and exposed to client via error callback
    func testThrowExceptionAfterCollectSuspend() {
        let expectation = XCTestExpectation(description: "throwExceptionAfterCollectSuspend")
        XCTAssertNoThrow(
            FlowErrorCollector().throwExceptionAfterCollectSuspend() { _, error in
                XCTAssert((error as Any) is NSError, "Returned error is not NSError")
                let kException = (error! as NSError).kotlinException
                XCTAssert(kException is FlowErrorCollectorException, "Returned exception is not FlowErrorCollectorException")
                expectation.fulfill()
            }
        )

        wait(for: [expectation], timeout: 5.0)
    }

    // exception *is* thrown before coroutine and exposed to client via error callback
    func testThrowExceptionFromFlowDuringCollectSuspend() {
        let expectation = XCTestExpectation(description: "throwExceptionFromFlowDuringCollectSuspend")
        XCTAssertNoThrow(
            FlowErrorCollector().throwExceptionFromFlowDuringCollectSuspend() { _, error in
                XCTAssert((error as Any) is NSError, "Returned error is not NSError")
                let kException = (error! as NSError).kotlinException
                XCTAssert(kException is FlowErrorEmitterException, "Returned exception is not FlowErrorEmitterException")
                expectation.fulfill()
            }
        )

        wait(for: [expectation], timeout: 5.0)
    }
}
