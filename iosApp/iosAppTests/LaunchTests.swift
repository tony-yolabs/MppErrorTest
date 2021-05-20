@testable import shared
import XCTest

// Tests with scope.launch
// - *None* of these examples expose exceptions to UI client

// - intra-coroutine exception handling (i.e. within a single coroutine) mirrors traditional try/catch

// - inter-coroutine exception handling is generally opaque:
// - uncaught exceptions are handled by top level coroutine handler in Kotlin, but cannot be propagated further
// - *without* a top level coroutine handler, uncaught exceptions will log and crash the app
// - nested coroutines propagate uncaught exceptions via CancellationException
//   - when run in a normal (non-supervisoryScope), uncaught exceptions are propagated as follows:
//      - exception is propagated up to parent
//        - parent will cancel children
//        - then cancel self
//        - then exception is further propagated up to parent
//   - when run in a supervisoryScope, uncaught exceptions are only propagated down the coroutine tree

class LaunchTests: XCTestCase {
    func testNoThrow() {
        let ret = LaunchError().noThrow()
        XCTAssertTrue(ret)
    }
}

// caught inside coroutine ... not exposed
class LaunchTestsIntra: XCTestCase {
    func testIntraCaught() {
        XCTAssertNoThrow(
            LaunchError().intraCaught()
        )
    }
}


// caught inside coroutine ... rethrown ... uncaught ... not exposed
class LaunchTestsIntra_UncaughtRethrow: XCTestCase {
    func testIntraUncaughtRethrow() {
        XCTAssertNoThrow(
            LaunchError().intraUncaughtRethrow()
        )
    }
}

// caught inside coroutine ... rethrown ... caught ... not exposed
class LaunchTestsIntra_UncaughtRethrowHandler: XCTestCase {
    func testIntraUncaughtRethrowHandler() {
        XCTAssertNoThrow(
            LaunchError().intraUncaughtRethrowHandler()
        )
    }
}

// not caught inside coroutine ... but still not exposed
class LaunchTestsIntra_Uncaught_1: XCTestCase {
    // uncaught IllegalStateException
    func testBaseThrowNoHandlerUncaught() {
        XCTAssertNoThrow(
            LaunchError().intraNestingErrorUncaught()
        )
    }
}

class LaunchTests_Uncaught_1: XCTestCase {
    // uncaught IllegalStateException
    func testBaseThrowNoHandlerUncaught() {
        XCTAssertNoThrow(
            try LaunchError().baseThrowNoHandlerUncaught()
        )
    }
}

class LaunchTests_Uncaught_2: XCTestCase {
    // uncaught LaunchErrorCustomException
    func testNestedThrowNoHandlerUncaught() {
        XCTAssertNoThrow(
            try LaunchError().nestedThrowNoHandlerUncaught()
        )
    }
}

class LaunchTests_Uncaught_3: XCTestCase {
    // uncaught LaunchErrorCustomException
    func testNestedThrowWithTopHandlerUncaught() {
        XCTAssertNoThrow(
            try LaunchError().nestedThrowWithTopHandlerUncaught()
        )
    }
}

class LaunchTests_Uncaught_4: XCTestCase {
    // uncaught LaunchErrorCustomException
    func testNestedThrowWithInnerHandler() {
        XCTAssertNoThrow(
            try LaunchError().nestedThrowWithInnerHandler()
        )
    }

}

class LaunchTests_Uncaught_5: XCTestCase {
    // uncaught LaunchErrorCustomException
    func testNestedMultpleWithHandlersCaught() {
        XCTAssertNoThrow(
            try LaunchError().nestedMultpleWithHandlersCaught()
        )
    }

}

class LaunchTests_Caught: XCTestCase {
    // catches IllegalStateException
    func testBaseThrowWithHandler() {
        XCTAssertNoThrow(
            try LaunchError().baseThrowWithHandler()
        )
    }


    // caught LaunchErrorCustomException
    func testNestedThrowWithTopHandlerCaught() {
        XCTAssertNoThrow(
            try LaunchError().nestedThrowWithTopHandlerCaught()
        )
    }

    // catches LaunchErrorCustomException
    func testNestedThrowWithHandlers() {
        XCTAssertNoThrow(
            try LaunchError().nestedThrowWithHandlers()
        )
    }
}

class LaunchTestsTree_Uncaught_1: XCTestCase {
    func testTreeCoroutinesThrowsNoHandlerBeforeUncaught() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsNoHandlerBeforeUncaught")

        LaunchError().treeCoroutinesThrowsNoHandlerBeforeUncaught() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(1, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }
}

class LaunchTestsTree: XCTestCase {
    func testTreeCoroutinesNoHandlers() {
        let expectation = XCTestExpectation(description: "treeCoroutinesNoHandlers")

        LaunchError().treeCoroutinesNoHandlers() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(2, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }


    func testTreeCoroutinesThrowsWithTopHandlerBeforeCaught() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsWithTopHandlerBeforeCaught")

        LaunchError().treeCoroutinesThrowsWithTopHandlerBeforeCaught() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(1, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    func testTreeCoroutinesThrowsWithBothHandlersBefore() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsWithBothHandlersBefore")

        LaunchError().treeCoroutinesThrowsWithBothHandlersBefore() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(1, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    func testTreeCoroutinesThrowsWithBothHandlersAfter() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsWithBothHandlersAfter")

        LaunchError().treeCoroutinesThrowsWithBothHandlersAfter() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception after completion
            XCTAssertEqual(2, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    func testTreeCoroutinesThrowsWithBothHandlersBeforeCancellation() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsWithBothHandlersBeforeCancellation")

        LaunchError().treeCoroutinesThrowsWithBothHandlersBeforeCancellation() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling waits, the other throws an exception prior to completion
            XCTAssertEqual(0, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    func testTreeCoroutinesThrowsWithBothHandlersAfterCancellation() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsWithBothHandlersAfterCancellation")

        LaunchError().treeCoroutinesThrowsWithBothHandlersAfterCancellation() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling waits, the other throws an exception after completion
            XCTAssertEqual(1, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    func testTreeCoroutinesExplicitCancellation() {
        let expectation = XCTestExpectation(description: "treeCoroutinesThrowsWithBothHandlersAfterCancellation")

        LaunchError().treeCoroutinesExplicitCancellation() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // both siblings are cancelled prior to completion
            XCTAssertEqual(0, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }
}


class LaunchTestsSibling_Uncaught_1: XCTestCase {
    // catches LaunchErrorCustomException
    func testSiblingCoroutinesThrowsNoHandlerBeforeUncaught() {
        let expectation = XCTestExpectation(description: "siblingCoroutinesThrowsNoHandlerBeforeUncaught")

        LaunchError().siblingCoroutinesThrowsNoHandlerBeforeUncaught() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(1, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }
}

class LaunchTestsSibling_Uncaught_2: XCTestCase {
    // catches LaunchErrorCustomException
    func testSiblingCoroutinesThrowsNoHandlerAfterUncaught() {
        let expectation = XCTestExpectation(description: "siblingCoroutinesThrowsNoHandlerAfterUncaught")

        LaunchError().siblingCoroutinesThrowsNoHandlerAfterUncaught() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception after to completion
            XCTAssertEqual(2, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }
}

class LaunchTestsSiblingNoHandler: XCTestCase {

    func testSiblingCoroutinesNoHandlers() {
        let expectation = XCTestExpectation(description: "testSiblingCoroutinesNoHandlers")

        LaunchError().siblingCoroutinesNoHandlers() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(2, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

}

class LaunchTestsSiblingWithHandler: XCTestCase {

    // catches LaunchErrorCustomException
    func testSiblingCoroutinesThrowsWithHandlerBefore() {
        let expectation = XCTestExpectation(description: "siblingCoroutinesThrowsWithHandlerBefore")

        LaunchError().siblingCoroutinesThrowsWithHandlerBefore() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception prior to completion
            XCTAssertEqual(1, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    // catches LaunchErrorCustomException
    func testSiblingCoroutinesThrowsWithHandlerAfter() {
        let expectation = XCTestExpectation(description: "siblingCoroutinesThrowsWithHandlerAfter")

        LaunchError().siblingCoroutinesThrowsWithHandlerAfter() { ret, error in
            XCTAssertNotNil(ret, "No return value present.")
            XCTAssertNil(error, "Error was returned where none was expected")
            // one sibling completes, the other throws an exception after to completion
            XCTAssertEqual(2, ret)
            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }
}
