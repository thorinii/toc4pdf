package me.lachlanap.toc4pdf.ui

import java.awt.event._
import javax.swing._

import me.lachlanap.toc4pdf.proc.GeneratorListener

object UIHelpers {
  def setLaF = {
    def lafIsCrossPlatfrom = UIManager.getCrossPlatformLookAndFeelClassName == UIManager.getSystemLookAndFeelClassName

    val lafClass = UIManager.getInstalledLookAndFeels
    .find(_.getClassName.toLowerCase.contains("gtk"))
    .filter(_ => lafIsCrossPlatfrom)
    .map(_.getClassName)
    .getOrElse(UIManager.getCrossPlatformLookAndFeelClassName)

    UIManager.setLookAndFeel(lafClass)
  }

  def label(text: String, fontMult: Float = 1f, centre: Boolean = false): JLabel = {
    val label = new JLabel(text)

    if (fontMult != 1) {
      val font = label.getFont
      label.setFont(font.deriveFont(font.getSize * fontMult))
    }

    if(centre)
      label.setHorizontalAlignment(SwingConstants.CENTER)

    label
  }

  def button(text: String, listener: => Unit = ()): JButton = {
    val button = new JButton(text)
    button.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent): Unit = {
          listener
        }
      })

    button
  }

  def checkbox(text: String, selected: Boolean = false): JCheckBox = {
    new JCheckBox(text, selected)
  }

  def combo[T](contents: List[T]) = {
    val combo = new JComboBox[T]

    val model = new DefaultComboBoxModel[T]
    contents.foreach(item => model.addElement(item))
    combo.setModel(model)

    combo
  }

  def progressBar: JProgressBar = {
    val bar = new JProgressBar
    bar
  }

  def wrap(delegate: GeneratorListener) = {
    new GeneratorListener {
      def starting(count: Int) = runLater { delegate.starting(count) }
      def reading(name: String, from: String) = runLater { delegate.reading(name, from) }
      def writing(name: String, to: String) = runLater { delegate.writing(name, to) }
      def finished(name: String) = runLater { delegate.finished(name) }
      def finishedAll = runLater { delegate.finishedAll }

      private def runLater(r: => Unit): Unit = {
        SwingUtilities.invokeLater(new Runnable { def run = r })
      }
    }
  }

  def directoryChooser(path: String) = {
    val chooser = new JFileChooser(path)
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    chooser
  }

  def fileChooserResult(chooser: JFileChooser)(result: Int) = {
    result match {
      case JFileChooser.APPROVE_OPTION => Some(chooser.getSelectedFile)
      case _ => None
    }
  }

  def showMsg(msg: String, parent: JFrame) = {
    JOptionPane.showMessageDialog(parent, msg, "ToC 2 PDF", JOptionPane.ERROR_MESSAGE)
  }
}
