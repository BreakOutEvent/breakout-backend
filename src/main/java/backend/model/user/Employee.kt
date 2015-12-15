@file:JvmName("Employee")

package backend.model.user

import javax.persistence.*

@Entity
@DiscriminatorValue("EMPLOYEE")
class Employee : UserRole {

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var address: Address? = null

    @Column(name = "emp_tshirtsize")
    var tshirtSize: String? = null
    var title: String? = null
    var phonenumber: String? = null

    constructor() : super() {
    }

    constructor(core: UserCore) : super(core) {
    }

    override fun getAuthority(): String = "EMPLOYEE"
}
