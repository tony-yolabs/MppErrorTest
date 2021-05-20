@testable import shared
import XCTest

// Tests with scope.async

class AsyncTests: XCTestCase {
    func testAsyncNoThrow() {
        AsyncError().noThrow() { ret, error in
            XCTAssert(ret == true)
            XCTAssertNil(error)
        }
    }

    func testAsyncThrows() {
        let expectation = XCTestExpectation(description: "testAsyncThrows")

        AsyncError().throwAsync() { ret, error in
            XCTAssertNil(ret, "Unexpected return value present.")
            XCTAssert((error as Any) is NSError, "Returned error is not NSError")

            let nsErrorCast = error! as NSError
            let kException = nsErrorCast.kotlinException as! KotlinException
            XCTAssert(kException is AsyncErrorCustomException, "Returned exception is not AsyncErrorCustomException")

            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }

    // note - await/throw is encapsulated in the deferred object ... not the async block
    //   so in this examples AsyncErrorCustomException2 is exposed as it is thrown first in the execution path
    func testAsyncThrows2() {
        let expectation = XCTestExpectation(description: "testAsyncThrows2")

        AsyncError().throwAsync2() { ret, error in
            XCTAssertNil(ret, "Unexpected return value present.")
            XCTAssert((error as Any) is NSError, "Returned error is not NSError")

            let nsErrorCast = error! as NSError
            let kException = nsErrorCast.kotlinException as! KotlinException
            XCTAssert(kException is AsyncErrorCustomException2, "Returned exception is not AsyncErrorCustomException2")

            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }



    // note - await/throw is encapsulated in the deferred object ... not the async block
    //   so in this examples AsyncErrorCustomException2 is exposed as it is thrown first in the execution path
    func testAsyncThrows2WithHandler() {
        let expectation = XCTestExpectation(description: "testAsyncThrows2WithHandler")

        AsyncError().throwAsync2WithHandler() { ret, error in
            XCTAssertNil(ret, "Unexpected return value present.")
            XCTAssert((error as Any) is NSError, "Returned error is not NSError")

            let nsErrorCast = error! as NSError
            let kException = nsErrorCast.kotlinException as! KotlinException
            XCTAssert(kException is AsyncErrorCustomException2, "Returned exception is not AsyncErrorCustomException2")

            expectation.fulfill()
        }

        wait(for: [expectation], timeout: 5.0)
    }
}
