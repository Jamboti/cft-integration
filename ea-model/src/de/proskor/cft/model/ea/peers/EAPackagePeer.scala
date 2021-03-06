package de.proskor.cft.model.ea.peers
import de.proskor.cft.model.ea._

class EAPackagePeer(val instance: cli.EA.IPackage) extends EAPeer {
  val id: Int = instance.get_PackageID

  def name = instance.get_Name.asInstanceOf[String]
  def name_=(name: String) = instance.set_Name(name)

  def stereotype: String = instance.get_StereotypeEx.asInstanceOf[String]
  def stereotype_=(stereotype: String) = instance.set_StereotypeEx(stereotype)

  private def elementKids: Set[cli.EA.IElement] = {
    val collection = instance.get_Elements.asInstanceOf[cli.EA.ICollection]
    val instances = for (i <- 0 until collection.get_Count) yield collection.GetAt(i.toShort).asInstanceOf[cli.EA.IElement]
    instances.toSet
  }

  private def packageKids: Set[cli.EA.IPackage] = {
    val collection = instance.get_Packages.asInstanceOf[cli.EA.ICollection]
    val instances = for (i <- 0 until collection.get_Count) yield collection.GetAt(i.toShort).asInstanceOf[cli.EA.IPackage]
    instances.toSet
  }

  def elements: Set[EAPeer] =
    (for (kid <- elementKids) yield new EAElementPeer(kid)) ++ (for (kid <- packageKids) yield new EAPackagePeer(kid))

  def elementsOfType(stereotypes: String*): Set[EAPeer] =
    for (kid <- elementKids if stereotypes.contains(kid.get_Stereotype)) yield new EAElementPeer(kid)

  def packages: Set[EAPeer] =
    for (kid <- packageKids) yield new EAPackagePeer(kid)

  def parent: EAPeer = 
    if (instance.get_ParentID > 0)
      new EAPackagePeer(EAFactory.pkg(instance.get_ParentID))
    else
      new EARepositoryPeer(EAFactory.repositoryPeer)

  def addPackage(name: String): EAPeer = {
    val collection = instance.get_Packages.asInstanceOf[cli.EA.ICollection]
    val pkgPeer = collection.AddNew(name, "Package").asInstanceOf[cli.EA.IPackage]
    pkgPeer.Update()
    collection.Refresh()
    EAFactory.cache += pkgPeer.get_PackageID -> pkgPeer
    new EAPackagePeer(pkgPeer)
  }

  def deletePackage(pkg: EAPeer) {
    val collection = instance.get_Packages.asInstanceOf[cli.EA.ICollection]
    var i = 0
    var found = false
    while (i < collection.get_Count && !found) {
      if (collection.GetAt(i.toShort).asInstanceOf[cli.EA.IPackage].get_PackageID == pkg.asInstanceOf[EAPackagePeer].instance.get_PackageID)
        found = true
      else
        i += 1
    }
    if (found) {
      collection.Delete(i.toShort)
      collection.Refresh()
      EAFactory.cache -= pkg.id
    }
  }

  def addElement(name: String, stereotype: String): EAPeer = {
    val collection = instance.get_Elements.asInstanceOf[cli.EA.ICollection]
    val elPeer = collection.AddNew(name, "Object").asInstanceOf[cli.EA.IElement]
    elPeer.set_StereotypeEx(stereotype)
    elPeer.Update()
    collection.Refresh()
    EAFactory.cache += elPeer.get_ElementID -> elPeer
    new EAElementPeer(elPeer)
  }

  def deleteElement(element: EAPeer) {
    val collection = instance.get_Elements.asInstanceOf[cli.EA.ICollection]
    var i = 0
    var found = false
    while (i < collection.get_Count && !found) {
      if (collection.GetAt(i.toShort).asInstanceOf[cli.EA.IElement].get_ElementID == element.asInstanceOf[EAElementPeer].instance.get_ElementID)
        found = true
      else
        i += 1
    }
    if (found) {
      collection.Delete(i.toShort)
      collection.Refresh()
      EAFactory.cache -= element.id
    }
  }

  def connectedElements: Set[EAPeer] = Set[EAPeer]()
  def connect(element: EAPeer) {}
  def disconnect(element: EAPeer) {}
}