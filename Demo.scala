package demo

import monocle.{Lens,Prism,Optional}
import monocle.macros.GenLens

sealed trait Occupation
case object PokemonTrainer extends Occupation
case class Programmer( company: String ) extends Occupation

case class Address( number: Int, streetName: String, suburb: String)
case class Person( name:String, age: Int, address: Address, occupation: Occupation )

object lens  {

  val testPerson = Person(
    name       = "Ben Kolera",
    age        = 29,
    address    = Address(225, "Montague Road","West End"),
    occupation = Programmer("Ephox")
  )

  val testPerson2 = Person(
    name       = "Ash Ketchum",
    age        = 10,
    address    = Address(1,"Route 1","Pallet Town"),
    occupation = PokemonTrainer
  )

  // But with immutable data, updating the nested structures underneath can be
  // a real pain.

  def updatePersonSuburb(p:Person,s:String) = {
    p.copy(
      address = p.address.copy( suburb = s )
    )
  }

  // Enter Lenses, which wrap up the getter and setter into a first class
  // composable thing.

  val _personName = Lens[Person,String](
    _.name                                //Getter
  )(
    n => p => p.copy( name = n )          //Setter
  )

  def testGetPersonName = _personName.get(testPerson)
  // String = Ben Kolera

  def testSetPersonName = _personName.modify(_.toUpperCase)(testPerson)
  // monocledemo.Person = Person(B. Kolera,29,Address(225,Montague Road,West End),Programmer(Ephox))
  // testPerson still returns the initial value

  // There is an easier way to define the lenses

  val _personAge = GenLens[Person](_.age)
  val _personAddress = GenLens[Person](_.address)
  val _personOccupation = GenLens[Person](_.occupation)

  val _addressNumber = GenLens[Address](_.number)
  val _addressStreetName = GenLens[Address](_.streetName)
  val _addressSuburb = GenLens[Address](_.suburb)

  // But the coolest part is that we can compose the lenses together
  val _personStreetName: Lens[Person,String] =
    _personAddress composeLens _addressStreetName

  def testGetPersonStreetName = _personStreetName.get(testPerson)
  // String = Montague Road
  def testSetPersonStreetName = _personStreetName.set("1 Road Road")(testPerson)
  // monocledemo.Demo.Person = Person(Ben Kolera,29,Address(225,1 Road Road,West End),Programmer(Ephox))

  // We also have a first class way of modelling the branches of sum types. Called Prisms
  // Remember our occupation had the following choices:
  //  - PokemonTrainer()
  //  - Programmer(company:String)
  val _occupationProgrammer = Prism[Occupation,String]{
    case Programmer(s) => Some(s)
    case _             => None
  }(
    Programmer.apply
  )

  // Which we can use to optionally grab a company from an Occupation
  def testOccupationProgrammerSome =
    _occupationProgrammer.getOption(Programmer("Ephox"))
  // Option[String] = Some(Ephox)
  def testOccupationProgrammerNone =
    _occupationProgrammer.getOption(PokemonTrainer)
  // Option[String] = None

  def testOccupationProgrammerModifySome =
    _occupationProgrammer.modify(_.toUpperCase)(Programmer("Ephox"))
  // monocledemo.Occupation = Programmer(EPHOX)

  def testOccupationProgrammerModifyNone =
    _occupationProgrammer.modify(_.toUpperCase)(PokemonTrainer)
  // monocledemo.Occupation = PokemonTrainer

  def testOccupationReverseGet =
    _occupationProgrammer.reverseGet("Ephox")
  // monocledemo.Occupation = Programmer(Ephox)

  // We can compose these with getters!
  val _personCompany:Optional[Person,String] = _personOccupation composePrism _occupationProgrammer
  // But the result isn't a Prism since we lose the ability to reverseGet
  // We do however, retain getOption and modify :)
  def testPersonCompanyGetSome =
    _personCompany.getOption(testPerson)
  // Option[String] = Some(Ephox)
  def testPersonCompanyGetNone =
    _personCompany.getOption(testPerson2)
  // Option[String] = None

  def testPersonCompanyModifySome =
    _personCompany.modify(_.toUpperCase)(testPerson)
  // monocledemo.Demo.Person = Person(Ben Kolera,29,Address(225,1 Road Road,West End),Programmer(EPHOX))

  def testPersonCompanyModifyNone =
    _personCompany.modify(_.toUpperCase)(testPerson2)
  // demo.Person = Person(Ash Ketchum,10,Address(1,Route 1,Pallet Town),PokemonTrainer)

  // Take a loot at the heirarchy here: https://camo.githubusercontent.com/0e7ccc1c6f603384b4051eda1b9e1169ee305c31/68747470733a2f2f7261772e6769746875622e636f6d2f6a756c69656e2d74727566666175742f4d6f6e6f636c652f6d61737465722f696d6167652f636c6173732d6469616772616d2e706e67

  // Iso is a bidirectional mapping between the types Iso[List,IList]
  // Traversal is an optional with 0 or many targets.
  // Getter is just the getter part of a Lens
  // Setter is just the setter part of a Lens/Prism/Optional/Traversal
  // Fold is a Getter of 0 or more values.

  // This gives a generalised first class set of structures to model access into
  // your data.

  // Defining Optics is useful just for dealing with immutable data, but it also
  // allows you to abstract away parts of your structure by hiding user access to
  // data via composed lenses like _personCompany.

}
