package me.lachlanap.toc4pdf.ui

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Path
import javax.swing._
import me.lachlanap.toc4pdf.model._
import me.lachlanap.toc4pdf.proc._
import UIHelpers._

class MainFrame {
  private val frame: JFrame = new JFrame("ToC 4 PDF")

  private var generateBtn: JButton = _
  private var generatorListener: GeneratorListener = _

  private var keepOldBookmarksChk: JCheckBox = _
  private var forceZoomCmbo: JComboBox[Zoom] = _

  private var filesList: JTextArea = _
  private var titlesList: JTextArea = _

  private var filesMapping: Map[String, File] = Map.empty
  private var outputDirectory: File = _

  buildFrame(frame)

  def show = {
    frame.setVisible(true)
  }

  private def buildFrame(frame: JFrame) = {
    val controlsPanel = new JPanel
    buildControls(controlsPanel)

    val filesListPanel = new JPanel
    buildFilesList(filesListPanel)

    val actionsPanel = new JPanel
    buildActions(actionsPanel)

    val mainPanel = new JPanel
    mainPanel.setLayout(new BorderLayout)

    mainPanel.add(controlsPanel, BorderLayout.NORTH)
    mainPanel.add(filesListPanel, BorderLayout.CENTER)
    mainPanel.add(actionsPanel, BorderLayout.SOUTH)

    frame.getContentPane.add(mainPanel)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setLocationByPlatform(true)
    frame.pack
    frame.setMinimumSize(frame.getSize)
  }

  private def buildControls(panel: JPanel) = {
    panel.setLayout(new GridBagLayout)

    keepOldBookmarksChk = checkbox("Keep Existing Bookmarks", true)
    panel.add(keepOldBookmarksChk, GBC(1, 0, fill = Fill.Horizontal, weightX = 1).toGBC)

    forceZoomCmbo = combo(Zoom.AutoZooms)
    panel.add(label("Force Page Zoom:"), GBC(0, 1, anchor = Anchor.East).toGBC)
    panel.add(forceZoomCmbo, GBC(1, 1, fill = Fill.Horizontal, weightX = 1).toGBC)
  }

  private def buildFilesList(panel: JPanel) = {
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    panel.setLayout(new BorderLayout);

    val labelsPanel = new JPanel
    val listsPanel = new JPanel

    labelsPanel.setLayout(new GridLayout(0, 2, 5, 5));
    listsPanel.setLayout(new GridLayout(0, 2, 5, 5));
    panel.add(labelsPanel, BorderLayout.NORTH)
    panel.add(listsPanel, BorderLayout.CENTER)

    labelsPanel.add(label("PDF Files", centre = true))
    labelsPanel.add(label("Titles", centre = true))

    filesList = new JTextArea
    listsPanel.add(new JScrollPane(filesList))

    titlesList = new JTextArea
    listsPanel.add(new JScrollPane(titlesList))
  }

  private def buildActions(panel: JPanel) = {
    panel.setLayout(new GridBagLayout)

    val loadBtn = button("Load from...", loadFrom)
    val saveBtn = button("Save to...", saveTo)
    panel.add(loadBtn, GBC(0, 0, fill = Fill.Horizontal, weightX = 1).toGBC)
    panel.add(saveBtn, GBC(1, 0, fill = Fill.Horizontal, weightX = 1).toGBC)

    generateBtn = button("Generate", generate)
    generateBtn.setEnabled(false)
    panel.add(generateBtn, GBC(0, 1, width = 2, fill = Fill.Horizontal, weightX = 1).toGBC)

    val generateBar = progressBar
    generateBar.setStringPainted(true)
    generateBar.setString("")
    panel.add(generateBar, GBC(0, 2, width = 2, fill = Fill.Horizontal, weightX = 1).toGBC)

    generatorListener = wrap(new GeneratorListener {
        var count: Int = 0
        var done: Int = 0

        def starting(c: Int) = {
          count = c
          done = 0

          generateBtn.setEnabled(false)

          generateBar.setMaximum(count * 2)
          generateBar.setValue(0)
          generateBar.setString("")
        }

        def reading(name: String, from: String) = {
          generateBar.setValue(done * 2)
          generateBar.setString("Reading " + name)
        }

        def writing(name: String, to: String) = {
          generateBar.setValue(done * 2 + 1)
          generateBar.setString("Writing " + name)
        }

        def finished(name: String) = {
          done += 1
        }

        def finishedAll = {
          generateBtn.setEnabled(true)

          generateBar.setValue(count * 2)
          generateBar.setString("Done")
        }
      })
  }

  private def loadFrom = {
    val chooser = directoryChooser(".")
    fileChooserResult(chooser)(chooser.showOpenDialog(frame)) match {
      case None =>

      case Some(directory) =>
        val pdfs = directory.listFiles(
          new FilenameFilter() {
            def accept(parent: File, name: String) = {
              name.endsWith(".pdf")
            }
          }
        )

        filesMapping = pdfs.map(f => f.getName -> f).toMap
        filesList.setText(pdfs.map(_.getName).sorted.mkString("\n"))
    }
  }

  private def saveTo = {
    val chooser = directoryChooser(".")
    fileChooserResult(chooser)(chooser.showOpenDialog(frame)) match {
      case None =>
        generateBtn.setEnabled(false)

      case Some(dir) =>
        outputDirectory = dir
        generateBtn.setEnabled(true)
    }
  }

  private def generate = {
    val filenames = filesList.getText.split("\n").toList.map(_.trim).filterNot(_.isEmpty)
    val titles = titlesList.getText.split("\n").toList.map(_.trim).filterNot(_.isEmpty)

    if(!filenames.isEmpty) {
      val files = filenames.map(filesMapping.get)
      if(files.exists(_.isEmpty)) {
        showMsg("Files: \n  " + filenames.filter(filesMapping.get(_).isEmpty).mkString("\n  ") + "\ndo not exist",
                frame)
      } else if(filenames.size != titles.size) {
        showMsg("Must have a corresponding title for each file", frame)
      } else if(outputDirectory == false) {
        showMsg("Must have an output directory", frame)
      } else {
        val contentsList = titles.zip(files.flatMap(f => f)).map(t => Document(t._1, t._2.getAbsoluteFile.toPath.toAbsolutePath))

        val contents = Contents(contentsList)
        val config = GeneratorConfig(
          keepOldBookmarks = keepOldBookmarksChk.isSelected,
          forceZoom = forceZoomCmbo.getSelectedItem.asInstanceOf[Zoom],
          output = outputDirectory.toPath
        )

        new Thread(new Runnable {
            def run = {
              new ContentsGenerator(contents, config, generatorListener).process
            }
          }).start
      }
    }
  }
}
