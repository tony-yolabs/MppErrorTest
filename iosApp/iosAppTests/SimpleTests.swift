@testable import shared
import XCTest

class IosAppTests: XCTestCase {
    func testGreeting() throws {
        let e = Greeting().greeting()
        print(e)
        XCTAssertNotNil(e)
    }
}

class SimpleTests: XCTestCase {

    // nothing to do, nothing thrown
    func testNoThrow() {
        XCTAssertNoThrow(SimpleError().noThrow())
    }

    // throws a standard exception
    // both the bridged NSError and the original KotlinException are exposed
    func testBaseThrow() {
        XCTAssertThrowsError(
            try SimpleError().baseThrow()
        ) { error in
            XCTAssert((error as Any) is NSError, "Returned error is not NSError")
            let kException = (error as NSError).kotlinException
            XCTAssert(kException is KotlinIllegalStateException, "Returned exception is not KotlinIllegalStateException")
        }
    }

    // throws a custom exception
    func testCustomThrow() {
        XCTAssertThrowsError(
            try SimpleError().customThrow()
        ) { error in
            XCTAssert((error as Any) is NSError, "Returned error is not NSError")
            let kException = (error as NSError).kotlinException
            XCTAssert(kException is SimpleCustomException, "Returned exception is not SimpleCustomException")
        }
    }

    // testing nested exceptions
    // ... but the nested exception is not bridged, only exposed through the original KotlinException
    func testCustomThrowNested() {
        XCTAssertThrowsError(
            try SimpleError().customThrowNested()
        ) { error in
            XCTAssert((error as Any) is NSError, "Returned error is not NSError")
            let nsErrorCast = error as NSError

            // description available ...
            XCTAssertEqual("Inner: customThrowNested", nsErrorCast.localizedDescription)

            if #available(iOS 14.5, *) {
                // ... but kotlin/OnjC bridge does *not* expose nested exceptions
                XCTAssert(nsErrorCast.underlyingErrors.isEmpty)
            }

            // so let's cast back the underlying kException ...
            XCTAssert(nsErrorCast.kotlinException is SimpleCustomException, "Returned exception is not SimpleCustomException")
            let kException = nsErrorCast.kotlinException as! KotlinException

            // ... and now we can access the nested exception
            let innerKException = kException.cause as! KotlinException
            XCTAssert(innerKException is SimpleInnerException, "Nested exception is not SimpleInnerException")
        }
    }
}
